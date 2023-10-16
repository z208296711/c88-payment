package com.c88.payment.service.thirdparty;

import com.alibaba.fastjson.JSON;
import com.c88.common.core.enums.RemitStateEnum;
import com.c88.common.core.result.ResultCode;
import com.c88.common.redis.utils.RedisUtils;
import com.c88.common.web.exception.BizException;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.CallBackFrom;
import com.c88.payment.pojo.form.DipperPayCallBackForm;
import com.c88.payment.pojo.form.DipperPayOutCallBackForm;
import com.c88.payment.service.CallBackService;
import com.c88.payment.service.IMemberRechargeService;
import com.c88.payment.service.IMemberWithdrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.WITHDRAW_FAIL;
import static com.c88.payment.constants.RedisKey.DAILY_WITHDRAW_AMOUNT;


@Slf4j
@Service("dipperCallBackService")
@RequiredArgsConstructor
public class DipperCallBackServiceImpl {

    private final RedisTemplate<String, Object> redisTemplate;

    private final IMemberRechargeService iRechargeService;

    private final IMemberWithdrawService iWithdrawService;

    private final IMemberRechargeService iMemberRechargeService;

    private final CallBackService callBackService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 成功
     */
    private static final Integer ORDER_SUCCESS = 1;

    private static final String SUCCESS_RESULT = "success";


    public void callBack(DipperPayCallBackForm dipperPayCallBackForm) {
        log.info("=====dipper form pay : {}", dipperPayCallBackForm);

        // 回傳狀態成功或失敗才繼續
        if (!ORDER_SUCCESS.equals(dipperPayCallBackForm.getTradeStatus())) {
            return;
        }

        // 寫入redis避免短時間重複執行
        if (Boolean.TRUE.equals(redisTemplate.hasKey(dipperPayCallBackForm.getSign()))) {
            return;
        }
        redisTemplate.opsForValue().set(dipperPayCallBackForm.getSign(), Boolean.TRUE, 10, TimeUnit.SECONDS);

        // 檢查支付的資料
        if (Boolean.FALSE.equals(checkData(dipperPayCallBackForm))) {
            return;
        }


        CallBackFrom callBackFrom = CallBackFrom.builder()
                .transactionId(String.valueOf(dipperPayCallBackForm.getTradeNo()))
                .orderId(dipperPayCallBackForm.getTradeNo())
                .amount(dipperPayCallBackForm.getTopupAmount())
                .realAmount(dipperPayCallBackForm.getTopupAmount())
                .note(dipperPayCallBackForm.getMessage())
                .build();

        // 訂單支付成功
        if (Objects.equals(dipperPayCallBackForm.getTradeStatus(), ORDER_SUCCESS)) {
            callBackService.orderSuccessProcess(callBackFrom);
            return;
        }

        // 訂單支付失敗
        callBackService.orderFailProcess(callBackFrom);
    }


    public void callBackPay(DipperPayOutCallBackForm form) {
        log.info("=====dipper form pay : {}", form);

        // 寫入redis避免短時間重複執行
        if (Boolean.TRUE.equals(redisTemplate.hasKey(form.getSign()))) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        redisTemplate.opsForValue().set(form.getSign(), Boolean.TRUE, 30, TimeUnit.SECONDS);


        MemberWithdraw memberWithdraw = iWithdrawService.lambdaQuery()
                .eq(MemberWithdraw::getWithdrawNo, form.getTradeNo())
                .eq(MemberWithdraw::getRemitState, RemitStateEnum.PAY_PENDING.getState())
                .oneOpt()
                .orElseThrow(() -> new BizException("dipper 無此代付訂單"));

        // 代付訂單成功
        if (Objects.equals(form.getTradeStatus(), ORDER_SUCCESS)) {

            iWithdrawService.lambdaUpdate()
                    .eq(MemberWithdraw::getWithdrawNo, memberWithdraw.getWithdrawNo())
                    .set(MemberWithdraw::getRemitState, RemitStateEnum.PAY_SUCCESS.getState())
                    .set(MemberWithdraw::getRemitNote, "自動代付成功")
                    .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                    .update();
            return ;
        }

        // 代付訂單失敗
        log.info("Dipper代付失敗 fail after {}", JSON.toJSONString(form));

        // 返回會員當前等級提款上限金額
        String key = RedisUtils.buildKey(DAILY_WITHDRAW_AMOUNT, memberWithdraw.getGmtCreate().toLocalDate(), memberWithdraw.getUid());
        BigDecimal memberDailyAmount = (BigDecimal) redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(memberDailyAmount)) {
            redisTemplate.opsForValue().set(key, memberDailyAmount.subtract(memberWithdraw.getAmount()), 3, TimeUnit.DAYS);
        }

        boolean result = iWithdrawService.lambdaUpdate()
                .eq(MemberWithdraw::getWithdrawNo, form.getTradeNo())
                .set(MemberWithdraw::getRemitState, RemitStateEnum.PAY_FAILED.getState())
                .set(MemberWithdraw::getRemitNote, "自動代付失敗")
                .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                .update();

        if (result) {
            // 返回會員申請提款時預先扣除的餘額
            kafkaTemplate.send(BALANCE_CHANGE,
                    AddBalanceDTO.builder()
                            .memberId(memberWithdraw.getUid())
                            .balance(memberWithdraw.getAmount())
                            .balanceChangeTypeLinkEnum(WITHDRAW_FAIL)
                            .type(WITHDRAW_FAIL.getType())
                            .betRate(BigDecimal.ZERO)
                            .note(WITHDRAW_FAIL.getI18n())
                            .build()
            );
        }

    }

    private Boolean checkData(DipperPayCallBackForm form) {
        return Boolean.TRUE;
    }
}

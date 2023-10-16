package com.c88.payment.service.thirdparty;

import com.c88.common.core.enums.RemitStateEnum;
import com.c88.common.redis.utils.RedisUtils;
import com.c88.common.web.exception.BizException;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.CallBackFrom;
import com.c88.payment.pojo.form.CallBackGeneralForm;
import com.c88.payment.pojo.form.CallBackGeneralResult;
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

/**
 * @description: TODO
 * @author: marcoyang
 * @date: 2023/1/3
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FengYangCallBackImpl implements ICallBackService{
    private final RedisTemplate<String, Object> redisTemplate;

    private final IMemberRechargeService iRechargeService;

    private final IMemberWithdrawService iWithdrawService;

    private final IMemberRechargeService iMemberRechargeService;

    private final CallBackService callBackService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 成功
     */
    private static final Integer ORDER_SUCCESS = 10000;

    private static final String SUCCESS_RESULT = "success";

    //支付
    @Override
    public String callBack(CallBackGeneralForm form) {
        CallBackGeneralResult result =  checkSignRedis(redisTemplate, form);
        CallBackFrom callBackFrom = CallBackFrom.builder()
                .transactionId(String.valueOf(result.getTransactionId()))
                .orderId(result.getOrderId())
                .amount(result.getAmount())
                .realAmount(result.getRealAmount())
                .note(result.getCustom())
                .build();

        // 訂單支付成功
        if (Objects.equals(form.getStatus(), ORDER_SUCCESS)) {
            return callBackService.orderSuccessProcess(callBackFrom);
        }

        // 訂單支付失敗
        return callBackService.orderFailProcess(callBackFrom);
    }

    //代付
    @Override
    public String callBackPay(CallBackGeneralForm form) {
        CallBackGeneralResult callBackResult =  checkSignRedis(redisTemplate, form);

        MemberWithdraw memberWithdraw = iWithdrawService.lambdaQuery()
                .eq(MemberWithdraw::getWithdrawNo, callBackResult.getOrderId())
                .eq(MemberWithdraw::getRemitState, RemitStateEnum.PAY_PENDING.getState())
                .oneOpt()
                .orElseThrow(() -> new BizException("FengYang 無此代付訂單"));

        // 代付訂單成功
        if (Objects.equals(form.getStatus(), ORDER_SUCCESS)) {
            log.info("FengYang代付成功 success before {}", callBackResult);

            iWithdrawService.lambdaUpdate()
                    .eq(MemberWithdraw::getWithdrawNo, memberWithdraw.getWithdrawNo())
                    .set(MemberWithdraw::getRemitState, RemitStateEnum.PAY_SUCCESS.getState())
                    .set(MemberWithdraw::getRemitNote, "自動代付成功")
                    .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                    .update();

            log.info("FengYang代付成功 success after {}", callBackResult);

            return SUCCESS_RESULT;
        }

        // 代付訂單失敗
        log.info("FengYang代付失敗 fail after {}", callBackResult);

        // 返回會員當前等級提款上限金額
        String key = RedisUtils.buildKey(DAILY_WITHDRAW_AMOUNT, memberWithdraw.getGmtCreate().toLocalDate(), memberWithdraw.getUid());
        BigDecimal memberDailyAmount = (BigDecimal) redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(memberDailyAmount)) {
            redisTemplate.opsForValue().set(key, memberDailyAmount.subtract(memberWithdraw.getAmount()), 3, TimeUnit.DAYS);
        }

        boolean result = iWithdrawService.lambdaUpdate()
                .eq(MemberWithdraw::getWithdrawNo, callBackResult.getOrderId())
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

        log.info("FengYang代付失敗 fail after {}", callBackResult);

        return SUCCESS_RESULT;
    }
}

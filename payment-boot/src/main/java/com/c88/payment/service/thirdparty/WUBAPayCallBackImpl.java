package com.c88.payment.service.thirdparty;

import com.c88.common.redis.utils.RedisUtils;
import com.c88.common.web.exception.BizException;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.CallBackGeneralForm;
import com.c88.payment.service.CallBackService;
import com.c88.payment.service.IMemberWithdrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.WITHDRAW_FAIL;
import static com.c88.payment.constants.RedisKey.DAILY_WITHDRAW_AMOUNT;

@Slf4j
@Service
@RequiredArgsConstructor
public class WUBAPayCallBackImpl implements ICallBackService {
    private final RedisTemplate<String, Object> redisTemplate;

    private final IMemberWithdrawService iWithdrawService;

    private final CallBackService callBackService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 成功
     */
    private static final Integer ORDER_SUCCESS = 5;

    private static final String SUCCESS_RESULT = "SUCCESS";

    //支付
    @Override
    public String callBack(CallBackGeneralForm form) {
        log.info("wubaPay form : {}", form);
        checkSignRedisNoConvert.accept(redisTemplate, form);
        callBcakHandler(form, callBackService, ORDER_SUCCESS);
        return SUCCESS_RESULT;
    }

    //代付
    @Override
    public String callBackPay(CallBackGeneralForm form) {
        log.info("wubaPayPay form : {}", form);
        checkSignRedisNoConvert.accept(redisTemplate, form);

        MemberWithdraw memberWithdraw = queryWithdraw.apply(iWithdrawService, form.getOrder_id())
                .orElseThrow(() -> new BizException("wubaPay 無此代付訂單"));

        // 代付訂單成功
        if (Objects.equals(form.getStatus(), ORDER_SUCCESS)) {
            log.info("wubaPay代付成功 success before {}", form);

            withDrawHandler(iWithdrawService, memberWithdraw.getWithdrawNo(), true);

            log.info("wubaPay代付成功 success after {}", form);

            return SUCCESS_RESULT;
        }

        // 代付訂單失敗
        log.info("wubaPay代付失敗 fail before {}", form);

        // 返回會員當前等級提款上限金額
        String key = RedisUtils.buildKey(DAILY_WITHDRAW_AMOUNT, memberWithdraw.getGmtCreate().toLocalDate(), memberWithdraw.getUid());
        BigDecimal memberDailyAmount = (BigDecimal) redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(memberDailyAmount)) {
            redisTemplate.opsForValue().set(key, memberDailyAmount.subtract(memberWithdraw.getAmount()), 3, TimeUnit.DAYS);
        }

        boolean result = withDrawHandler(iWithdrawService, form.getOrder_id(), false);

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

        log.info("wubaPay代付失敗 fail after {}", form);

        return SUCCESS_RESULT;
    }
}

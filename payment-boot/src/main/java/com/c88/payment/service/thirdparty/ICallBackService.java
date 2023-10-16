package com.c88.payment.service.thirdparty;

import com.alibaba.fastjson.JSON;
import com.c88.common.core.enums.RemitStateEnum;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.CallBackFrom;
import com.c88.payment.pojo.form.CallBackGeneralForm;
import com.c88.payment.pojo.form.CallBackGeneralResult;
import com.c88.payment.service.CallBackService;
import com.c88.payment.service.IMemberWithdrawService;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @description: TODO
 * @author: marcoyang
 * @date: 2023/1/3
 **/
public interface ICallBackService {
    String callBack(CallBackGeneralForm form);

    String callBackPay(CallBackGeneralForm form);

    default CallBackGeneralResult checkSignRedis(RedisTemplate redisTemplate, CallBackGeneralForm form) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(form.getSign()))) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        redisTemplate.opsForValue().set(form.getSign(), Boolean.TRUE, 10, TimeUnit.SECONDS);

        return JSON.parseObject(form.getResult(), CallBackGeneralResult.class);
    }

    default boolean callBcakHandler(CallBackGeneralForm form, CallBackService callBackService, Integer orderSuccess) {
        CallBackFrom callBackFrom = CallBackFrom.builder()
                .transactionId(form.getOrder_id())
                .orderId(form.getOrder_id())
                .amount(form.getAmount())
                .realAmount(form.getAmount())
                .note("")
                .build();

        // 訂單支付成功
        if (Objects.equals(form.getStatus(), orderSuccess)) {
            callBackService.orderSuccessProcess(callBackFrom);
            return true;
        }

        // 訂單支付失敗
        callBackService.orderFailProcess(callBackFrom);
        return false;
    }

    default boolean withDrawHandler(IMemberWithdrawService iWithdrawService, String withdrawNo, boolean isSuccess) {
        return iWithdrawService.lambdaUpdate()
                .eq(MemberWithdraw::getWithdrawNo, withdrawNo)
                .set(MemberWithdraw::getRemitState, isSuccess ? RemitStateEnum.PAY_SUCCESS.getState() : RemitStateEnum.PAY_FAILED.getState())
                .set(MemberWithdraw::getRemitNote, isSuccess ? "自動代付成功" : "自動代付失敗")
                .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                .update();
    }

    BiConsumer<RedisTemplate, CallBackGeneralForm> checkSignRedisNoConvert = (r, c) -> {
        if (Boolean.TRUE.equals(r.hasKey(c.getSign()))) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        r.opsForValue().set(c.getSign(), Boolean.TRUE, 10, TimeUnit.SECONDS);
    };

    BiFunction<IMemberWithdrawService, String, Optional<MemberWithdraw>> queryWithdraw = (i, o) ->
            i.lambdaQuery()
                    .eq(MemberWithdraw::getWithdrawNo, o)
                    .eq(MemberWithdraw::getRemitState, RemitStateEnum.PAY_PENDING.getState())
                    .oneOpt();


}

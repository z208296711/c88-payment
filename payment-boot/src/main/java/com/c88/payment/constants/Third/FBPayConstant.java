package com.c88.payment.constants.Third;

import java.util.Map;

public class FBPayConstant {

    private FBPayConstant() {
        throw new IllegalStateException("Utility class");
    }

    public static final String MERCHANT_CODE = "fbpay";

    public static final String SUCCESS = "0";

    /**
     * prepare	订单异常
     * error	商户提供参数错误
     * waiting	订单已建立，等待付款
     * mistake	平台出现错误
     * pending	需人工处理订单
     * success	已支付成功
     * success_done	支付并回调成功
     * expired	订单已过期
     */
    public static final Map<String, Integer> RECHARGE_STATE_MAP = Map.of(
            "prepare", 3,
            "error", 3,
            "waiting", 0,
            "mistake", 3,
            "pending", 2,
            "success", 1,
            "success_done", 1,
            "expired", 3
    );

}

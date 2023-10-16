package com.c88.payment.constants.thirdparty;

import java.util.Map;

public class WuBaPayConstant {

    private WuBaPayConstant() {
        throw new IllegalStateException("Utility class");
    }

    public static final String MERCHANT_CODE = "wubaPay";

    /**
     * 代收代付成功狀態
     */
    public static final Integer SUCCESS = 1;

    /**
     * 成功回調後回傳訊息
     */
    public static final String SUCCESS_CALL_BACK = "SUCCESS";

    /**
     * 交易狀態:
     * 0-错误
     * 1-等待中
     * 2,6-进行中
     * 3-失败
     * 5-成功。
     */
    public static final Map<String, Integer> ORDER_STATE_MAP = Map.of(
            "0", 3,
            "1", 0,
            "2", 2,
            "3", 3,
            "5", 1
    );

}

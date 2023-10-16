package com.c88.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 太子訂單狀態
 * 操作失败是 907 支付 独有的状态，泛指会员账号无法登 入、会员转账资料有误、会员余额不足等状态。
 */
@Getter
@AllArgsConstructor
public enum PrinceOrderStatus {

    NOT_PROCESSED(0, "未处理"),
    SUCCESS(1, "交易成功"),
    PROCESSING(2, "处理中"),
    TRANSACTION_FAIL(3, "交易失败"),
    OPERATE_FAIL(4, "操作失败"),
    QUERY_FAIL(5, "提单失败");

    private final Integer code;

    private final String label;

    public static PrinceOrderStatus getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }
}

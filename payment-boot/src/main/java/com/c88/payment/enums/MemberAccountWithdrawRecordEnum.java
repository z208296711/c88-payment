package com.c88.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 帳務紀錄提款狀態
 */
@Getter
@AllArgsConstructor
public enum MemberAccountWithdrawRecordEnum {

    ALL(0, "全部"),
    REVIEW(1, "審核中"),
    REJECTED(2, "審核拒絕"),
    WITHDRAWING(3, "出款中"),
    SUCCESS(4, "成功"),
    FAIL(5, "失敗");

    private final Integer code;

    private final String label;

    public static MemberAccountWithdrawRecordEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }
}

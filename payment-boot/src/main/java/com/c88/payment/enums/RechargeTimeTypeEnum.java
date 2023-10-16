package com.c88.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum RechargeTimeTypeEnum {

    RECEIVE(0, "申請時間"),
    TRANSACTION(1, "到帳時間");

    private final Integer code;

    private final String label;

    public static RechargeTimeTypeEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }

}

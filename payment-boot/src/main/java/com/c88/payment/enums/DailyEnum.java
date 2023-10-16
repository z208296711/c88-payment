package com.c88.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@AllArgsConstructor
@Getter
public enum DailyEnum {

    ASSIGN(0, "指定"),
    DAILY(1, "每日");

    private final Integer code;

    private final String label;

    public static DailyEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }

}

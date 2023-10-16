package com.c88.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@AllArgsConstructor
@Getter
public enum ChannelRechargeCodeEnum {

    G_CASH("gcash"),
    MAYA("maya"),
    QRCODE("qrcode");

    private final String code;

    public static ChannelRechargeCodeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }
}

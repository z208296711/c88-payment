package com.c88.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum MerchantRechargeTypeIsBank {

    NOT_BANK(0, "無銀行"),
    HAS_BANK(1, "有銀行");

    private final Integer code;

    private final String label;

    public static MerchantRechargeTypeIsBank getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }

}

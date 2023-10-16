package com.c88.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum SearchBankTypeEnum {

    ALL_BANK(0, "所有銀行"),
    MERCHANT_RECHARGE_USE_BANK(1, "商戶充值使用銀行");

    private final Integer code;

    private final String label;

    public static SearchBankTypeEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getCode(), code)).findFirst().orElseThrow();
    }

}

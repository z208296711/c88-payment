package com.c88.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum RechargeTypeEnum implements IBaseEnum<Integer> {

    MOMO(1, "momo"),
    ZALO(2, "zalo pay"),
    VIETTEL(3, "ViettelPay"),
    CARD_TRANS_TO_CARD(4, "卡轉卡"),
    ONLINE_PAYMENT(5, "直連"),
    QRCODE(6, "銀行轉碼支付"),
    COMPANY_BANK_CARD(7, "推薦卡轉卡"),
    USTD(8, "USTD");

    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private final Integer value;

    private final String label;

    public static RechargeTypeEnum getEnum(Integer value) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getValue(), value)).findFirst().orElseThrow();
    }

}

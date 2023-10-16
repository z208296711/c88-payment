package com.c88.payment.enums;

import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AffiliateBalanceChangeTypeEnum {

    //帳變類型 0:提款 1:代理轉帳 2:玩家轉帳 3:微調 4:返佣
    WITHDRAW(0, "提款"),
    AFFILIATE_TRANSFER(1, "代理轉帳"),
    MEMBER_TRANSFER(2, "玩家轉帳"),
    TUNING(3, "微調"),
    BACK_COMMISSION(4, "返佣"),
    ADMIN_RECHARGE(5, "管理員充值"),







    NOT_USE(99999, "佔位請勿使用");

    @Getter
    private final Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private final String label;

    AffiliateBalanceChangeTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    private static final Map<Integer, AffiliateBalanceChangeTypeEnum> map = Stream.of(values()).collect(Collectors.toMap(AffiliateBalanceChangeTypeEnum::getValue, Function.identity()));

    public static AffiliateBalanceChangeTypeEnum fromIntValue(int value) {
        return map.get(value);
    }

}

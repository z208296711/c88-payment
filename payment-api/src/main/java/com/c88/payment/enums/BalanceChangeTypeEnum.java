package com.c88.payment.enums;

import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BalanceChangeTypeEnum {

    //帳變類型  1:充值 2:提款 3:紅利：包含項目像是紅包、禮金、免費籌碼以及返水等等（不含存送優惠）4:清零：會員餘額低於某指定金額則清空該會員距離提現的總要求流水為0 5:轉帳：三方與平台間的金額移轉動作 6:佣金：代理佣金轉移 7:調整：手動調整累積要求流水 8:存送優惠
    RECHARGE(1, "充值"),
    WITHDRAW(2, "提款"),
    BONUS(3, "紅利"),
    ZERO(4, "清零"),
    TRANSFER(5, "轉帳"),
    COMMISSION(6, "佣金"),
    ADJUST(7, "調整"),
    RECHARGE_PROMOTION(8, "存送優惠");

    @Getter
    private final Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private final String label;

    BalanceChangeTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    private static final Map<Integer, BalanceChangeTypeEnum> map = Stream.of(values()).collect(Collectors.toMap(BalanceChangeTypeEnum::getValue, Function.identity()));

    public static BalanceChangeTypeEnum fromIntValue(int value) {
        return map.get(value);
    }

}

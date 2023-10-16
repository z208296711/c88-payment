package com.c88.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;

public enum MerchantStatusEnum implements IBaseEnum<Integer> {

    MERCHANT_CLOSE(0, "商戶關閉"),
    MERCHANT_OPEN(1, "商戶開啟"),
    RECHARGE_TYPE_STOP(2, "充值類型停用"),
    ALL_BANK_STOP(3, "所有銀行停用"),
    SOME_BANK_STOP(4, "部分銀行停用"),
    ALL_BANK_MAINTAIN(5, "所有銀行維護"),
    SOME_BANK_MAINTAIN(6, "部分銀行維護");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private final Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private final String label;

    MerchantStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}

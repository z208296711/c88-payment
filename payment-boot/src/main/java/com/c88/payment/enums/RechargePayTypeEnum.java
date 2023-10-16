package com.c88.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;

public enum RechargePayTypeEnum implements IBaseEnum<Integer> {

    NORMAL(0, "一般"),

    THIRD_PARTY(1, "充值銀行"),

    COMPANY_CARD(2, "自營卡");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private final Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private final String label;

    RechargePayTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}

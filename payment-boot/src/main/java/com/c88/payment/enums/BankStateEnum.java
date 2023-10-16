package com.c88.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;


public enum BankStateEnum implements IBaseEnum<Integer> {

    MAINTAINING(0, "維護中"),

    ENABLE(1, "啟用中"),

    SCHEDULED(2, "排程中");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private final Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private final String label;

    BankStateEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

}

package com.c88.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;

public enum ChannelTypeEnum implements IBaseEnum<Integer> {

    DISABLE(0, "不產生"),

    ENABLE(1, "產生");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private final Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private final String label;

    ChannelTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}

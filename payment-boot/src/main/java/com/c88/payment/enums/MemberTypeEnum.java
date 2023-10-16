package com.c88.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

public enum MemberTypeEnum {

    MEMBER(0, "會員"),

    AGENT(1, "代理");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private final Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private final String label;

    MemberTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}

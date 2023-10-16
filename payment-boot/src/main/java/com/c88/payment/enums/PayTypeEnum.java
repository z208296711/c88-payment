
package com.c88.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.Getter;


public enum PayTypeEnum implements IBaseEnum<Integer> {

    COMPANY_CARD(1, "自營卡"),

    THIRD_PARTY(2, "三方充值");

    @Getter
    @EnumValue //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    private final Integer value;

    @Getter
    // @JsonValue //  表示对枚举序列化时返回此字段
    private final String label;

    PayTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}

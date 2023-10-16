package com.c88.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum MemberRechargeTypeEnum implements IBaseEnum<Integer> {

    ONLINE(0, "三方回調","三方回調"),
    INLINE(1,"用戶充值", "手動充值(自營卡)"),
    INLINE_MAKE_UP(2,"手動補單", "手動充值-手動補單"),
    ONLINE_MAKE_UP(3, "手動補單","手動補單(商戶)");

    @EnumValue
    private final Integer value;

    private final String  name;

    private final String label;

    public static MemberRechargeTypeEnum getEnum(Integer value) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getValue(), value)).findFirst().orElseThrow();
    }

}

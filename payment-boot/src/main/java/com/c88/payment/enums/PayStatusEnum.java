
package com.c88.payment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.c88.common.core.base.IBaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum PayStatusEnum implements IBaseEnum<Integer> {

    IN_PROGRESS("in_progress", 0, "處理中"),
    SUCCESS("success", 1, "成功到帳"),
    FAIL("fail", 2, "充值失敗");

    private final String engDesc;
    @EnumValue
    private final Integer value;

    private final String label;

    public static PayStatusEnum getEnum(Integer value) {
        return Arrays.stream(values()).filter(filter -> Objects.equals(filter.getValue(), value)).findFirst().orElseThrow();
    }

}

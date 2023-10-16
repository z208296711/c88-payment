package com.c88.payment.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProtocolType {

    TRC20(1,"TRC20"),
    ERC20(2,"ERC20");

    /**
     * 協議編號
     */
    private final Integer no;

    /**
     * 協議名稱
     */
    private final String name;

}

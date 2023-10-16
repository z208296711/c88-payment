package com.c88.payment.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 漲變紀錄項目
 */
@Getter
@AllArgsConstructor
public enum LiqType {

    E001("001", "投注", 0),
    E002("002", "投注返点", 0),

    E101("101", "充值活动赠奖", 1),

    E201("201", "會員提款", 2),
    E202("202", "第三方入款", 2),
    E203("203", "管理员充值", 2),
    E204("204", "管理員扣除", 2),
    E205("205", "線下提款", 2);

    public static final Map<String, LiqType> liqTypes =
            Stream.of(values()).collect(Collectors.toMap(LiqType::getCode, Function.identity()));

    /**
     * 代碼
     */
    public final String code;

    /**
     * 項目名稱
     */
    public final String subName;

    /**
     * 項目名稱
     */
    public final Integer group;

    /**
     * 項目群組
     */
    private final String[] groupName = {"投注", "優惠", "系統"};

}

package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "自營卡轉卡-銀行卡管理")
public class CompanyBankCardVO {

    @Schema(title = "唯一值")
    private Integer id;

    @Schema(title = "代號")
    private String code;

    @Schema(title = "收款人名稱")
    private String owner;

    @Schema(title = "銀行卡號")
    private String bankCardNo;

    @Schema(title = "銀行別")
    private String bank;

    @Schema(title = "銀行Id")
    private Integer bankId;

    @Schema(title = "自營卡群組ID")
    private Integer groupId;

    @Schema(title = "自營卡群組名稱")
    private String group;

    @Schema(title = "單日存款上線總額")
    private BigDecimal dailyMaxAmount;

    @Schema(title = "單筆存款上限")
    private BigDecimal maxAmount;

    @Schema(title = "單筆存款下限")
    private BigDecimal minAmount;

    @Schema(title = "轉帳說明")
    private String note;

    @Schema(title = "是否產生附言 0:不產生, 1:產生")
    private Integer comments;

    @Schema(title = "啟用狀態 0:停用, 1:啟用")
    private Integer enable;

}

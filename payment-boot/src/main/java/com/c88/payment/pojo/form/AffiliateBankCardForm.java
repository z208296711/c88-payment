package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "新增-代理銀行卡")
public class AffiliateBankCardForm {

    @Schema(title = "id")
    private Long id;

    @NotNull(message = "真實名不得為空")
    @Schema(title = "真實名")
    private String realName;

    @NotNull(message = "銀行ID不得為空")
    @Schema(title = "銀行ID")
    private Long bankId;

    @NotNull(message = "卡號不得為空")
    @Schema(title = "卡號")
    private String cardNo;

    @Schema(title = "啟動狀態")
    private Integer enable;

}

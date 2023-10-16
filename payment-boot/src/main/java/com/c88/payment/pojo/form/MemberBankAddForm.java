package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "新增會員銀行")
public class MemberBankAddForm {

    @NotNull(message = "真實名不得為空")
    @Schema(title = "真實名")
    private String realName;

    @NotNull(message = "銀行ID不得為空")
    @Schema(title = "銀行ID")
    private Integer bankId;

    @NotNull(message = "卡號不得為空")
    @Schema(title = "卡號")
    private String cardNo;

}

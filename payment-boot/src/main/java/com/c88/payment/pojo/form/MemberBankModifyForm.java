package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "新增會員銀行")
public class MemberBankModifyForm {

    @Schema(title = "代理銀行ID")
    private Integer id;

    @Schema(title = "真實名")
    private String realName;

    @Schema(title = "銀行ID")
    private Integer bankId;

    @Schema(title = "卡號")
    private String cardNo;

    @Schema(title = "啟用狀態", description = "0停用1啟用")
    private Integer enable;

}

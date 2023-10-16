package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "查詢會員銀行表單")
public class MemberBankVO {

    @Schema(title = "ID")
    private Integer id;

    @Schema(title = "會員ID")
    private Integer memberId;

    @Schema(title = "銀行ID")
    private Integer bankId;

    @Schema(title = "真實名")
    private String realName;

    @Schema(title = "卡號")
    private String cardNo;

    @Schema(title = "啟用狀態", description = "0停用1啟用")
    private Integer enable;
}

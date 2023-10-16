package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(title = "新增充值訂單")
public class MemberRechargeFormVO {

    @Schema(title = "支付URL")
    private String payURL;

    @Schema(title = "充值金額")
    private BigDecimal amount;

    @Schema(title = "銀行別")
    private String bank;

    @Schema(title = "收款人")
    private String owner;

    @Schema(title = "銀行卡號")
    private String bankCardNo;

    @Schema(title = "轉帳說明")
    private String notes;

    @Schema(title = "附言")
    private String comment;
}

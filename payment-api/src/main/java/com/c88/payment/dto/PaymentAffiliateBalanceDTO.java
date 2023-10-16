package com.c88.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "會員餘額")
public class PaymentAffiliateBalanceDTO {

    @Schema(title = "ID")
    private Long id;

    @Schema(title = "代理ID")
    private Long affiliateId;

    @Schema(title = "代理帳號")
    private String username;

    @Schema(title = "代理金額")
    private BigDecimal balance;

}

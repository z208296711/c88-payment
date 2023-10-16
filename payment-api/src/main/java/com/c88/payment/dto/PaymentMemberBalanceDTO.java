package com.c88.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "會員餘額")
public class PaymentMemberBalanceDTO {

    @Schema(title = "ID")
    private Long id;

    @Schema(title = "會員ID")
    private Long memberId;

    @Schema(title = "會員帳號")
    private String username;

    @Schema(title = "金額")
    private BigDecimal balance;

}

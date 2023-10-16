package com.c88.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRechargeDTO {

    @Schema(title = "id")
    private Long id;

    @Schema(title = "內部單號")
    private String tradeNo;

    @Schema(title = "外部單號")
    private String outTradeNo;

    @Schema(title = "會員ID")
    private Long memberId;

    @Schema(title = "會員名稱")
    private String username;

    @Schema(title = "充值金額")
    private BigDecimal amount;

}

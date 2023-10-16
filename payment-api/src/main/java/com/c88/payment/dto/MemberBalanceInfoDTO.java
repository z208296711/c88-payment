package com.c88.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "會員錢包訊息")
public class MemberBalanceInfoDTO {

    @Schema(title = "會員ID")
    private Long memberId;

    @Schema(title = "會員帳號")
    private String username;

    @Schema(title = "首次充值時間")
    private LocalDateTime firstRechargeTime;

    @Schema(title = "最後充值時間")
    private LocalDateTime lastRechargeTime;

    @Schema(title = "首次提款時間")
    private LocalDateTime firstWithdrawTime;

    @Schema(title = "最後提款時間")
    private LocalDateTime lastWithdrawTime;
    @Schema(title = "首次充值金額")
    private BigDecimal firstRechargeBalance;

}

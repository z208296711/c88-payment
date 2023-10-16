package com.c88.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "增加餘額變動記錄")
public class AffiliateBalanceChangeRecordDTO implements Serializable {

    @NotNull(message = "代理ID不得為空")
    @Schema(title = "代理ID")
    private Long affiliateId;


    @NotNull(message = "變動金額")
    @Schema(title = "變動金額(平台分數)")
    private BigDecimal amount;

}
package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "前台-會員通道VO")
public class H5MemberChannelVO {

    @Schema(title = "通道id")
    private Long id;

    @Schema(title = "單筆充值最大金額")
    private BigDecimal maxRechargeAmount;

    @Schema(title = "單筆充值最小金額")
    private BigDecimal minRechargeAmount;

    @Schema(title = "手續費")
    private BigDecimal feeRate;

}

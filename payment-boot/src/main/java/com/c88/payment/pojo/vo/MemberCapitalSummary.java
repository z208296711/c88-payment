package com.c88.payment.pojo.vo;

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
@Schema(title = "資本概括")
public class MemberCapitalSummary {

    @Schema(title = "今日充值")
    private BigDecimal todayRechargeAmount;

    @Schema(title = "今日提款")
    private BigDecimal todayWithdrawAmount;

    @Schema(title = "累計充值")
    private BigDecimal totalRechargeAmount;

    @Schema(title = "累計提款")
    private BigDecimal totalWithdrawAmount;

    @Schema(title = "充值次數")
    private Integer rechargeCount;

    @Schema(title = "提款次數")
    private Integer withdrawCount;

    @Schema(title = "首存時間")
    private LocalDateTime firstRechargeTime;

    @Schema(title = "首提時間")
    private LocalDateTime firstWithdrawTime;

    @Schema(title = "末存時間")
    private LocalDateTime lastRechargeTime;

    @Schema(title = "末提時間")
    private LocalDateTime lastWithdrawTime;

    @Schema(title = "總存送優惠金額")
    private BigDecimal totalRechargeAwardAmount;

    @Schema(title = "總領取紅利金額")
    private BigDecimal totalAwardAmount;

    @Schema(title = "存送優惠筆數")
    private Integer rechargeAwardCount;

    @Schema(title = "紅利筆數")
    private Integer awardCount;

    @Schema(title = "提取控制", description = "0關閉 1開啟")
    private Integer withdrawControllerState;

}

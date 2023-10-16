package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CompanyReportVO {

    @Schema(title = "新註冊會員")
    Integer registerMemberCount;

    @Schema(title = "首存人數")
    Integer firstRechargeCount;

    @Schema(title = "首存金額")
    BigDecimal firstRechargeAmount;

    @Schema(title = "存款人數")
    Integer rechargeCount;

    @Schema(title = "存款金額")
    BigDecimal rechargeAmount;

    @Schema(title = "提款人數")
    Integer withdrawCount;

    @Schema(title = "提款金額")
    BigDecimal withdrawAmount;

    @Schema(title = "紅利人數")
    Integer awardCount;

    @Schema(title = "紅利金額")
    BigDecimal awardAmount;

    @Schema(title = "優惠人數")
    Integer rechargeAwardCount;

    @Schema(title = "優惠金額")
    BigDecimal rechargeAwardAmount;

    @Schema(title = "返水人數")
    Integer rebateCount;

    @Schema(title = "返水金額")
    BigDecimal rebateAmount;

    @Schema(title = "推廣佣金")
    BigDecimal promotionAmount = BigDecimal.ZERO;

    @Schema(title = "派彩金額")
    BigDecimal settleAmount;

    @Schema(title = "上分人數")
    Integer adminRechargeCount;

    @Schema(title = "上分金額")
    BigDecimal adminRechargeAmount;

    @Schema(title = "投注人數")
    Integer betCount;

    @Schema(title = "存款並投注人數")
    Integer rechargeAndBetCount;

    @Schema(title = "總投注額")
    BigDecimal allBetAmount;

    @Schema(title = "有效投注額")
    BigDecimal allValidBetAmount;

    @Schema(title = "公司輸贏")
    BigDecimal companyWinLoss;

    @Schema(title = "公司盈利")
    BigDecimal companyProfit;

    @Schema(title = "存提差額")
    BigDecimal diffAmount;
}

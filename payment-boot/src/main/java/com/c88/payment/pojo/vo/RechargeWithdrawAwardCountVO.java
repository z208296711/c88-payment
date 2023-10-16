package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class RechargeWithdrawAwardCountVO {
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
}

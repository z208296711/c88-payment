package com.c88.payment.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RechargeWithdrawTotalVO {
    BigDecimal mainCoin;
    Integer rechargeCount;
    BigDecimal rechargeAmount;
    BigDecimal rechargeFee;
    Integer withdrawCount;
    BigDecimal withdrawAmount;
    BigDecimal withdrawFee = BigDecimal.ZERO;
    BigDecimal totalFee;
}

package com.c88.payment.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawDistanceVO {
    //累計有效流水(用戶提款流水要求)
    BigDecimal accBet;

    //距離提現流水
    BigDecimal distance;
}

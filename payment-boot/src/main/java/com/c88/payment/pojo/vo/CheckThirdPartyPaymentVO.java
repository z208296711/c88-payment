package com.c88.payment.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckThirdPartyPaymentVO {

    private String transactionId;

    private String orderId;

    /**
     * 0:未处理
     * 1:交易成功
     * 2:处理中
     * 3:交易失败
     * 4:操作失败
     * 5:提单失败
     * 6:沖回
     */
    private Integer status;

    private BigDecimal amount;

    private BigDecimal realAmount;

}

package com.c88.payment.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdPartyBehalfPaymentVO {

    /**
     * 三方訂單號
     */
    private String transactionId;

}

package com.c88.payment.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdPartyPaymentVO {

    private String orderId;

    private String thirdPartyOrderId;

    private String payUrl;

    private BigDecimal amount;

}

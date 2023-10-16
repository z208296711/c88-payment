package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "支付資料表單")
public class CallBackFrom {

    @Schema(title = "三方訂單號")
    private String transactionId;

    @Schema(title = "訂單號")
    private String orderId;

    @Schema(title = "金额")
    private BigDecimal amount;

    @Schema(title = "實際存入金額")
    private BigDecimal realAmount;

    @Schema(title = "備註")
    private String note;

}

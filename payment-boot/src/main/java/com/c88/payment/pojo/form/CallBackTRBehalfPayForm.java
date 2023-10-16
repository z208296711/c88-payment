package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "TR代付回調表單")
public class CallBackTRBehalfPayForm {

    @Schema(title = "商户号")
    private String memberid;

    @Schema(title = "订单号")
    private String orderid;

    @Schema(title = "订单金额")
    private BigDecimal amount;

    @Schema(title = "交易状态( 2成功,3失败 )")
    private String refCode;

}

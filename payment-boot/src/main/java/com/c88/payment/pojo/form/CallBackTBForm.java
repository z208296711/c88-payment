package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "TB支付回調表單")
public class CallBackTBForm {

    @Schema(title = "平台订单号")
    private String trade_no;

    @Schema(title = "支付请求金额")
    private BigDecimal request_amount;

    @Schema(title = "支付实际金额")
    private BigDecimal amount;

    @Schema(title = "商户订单号")
    private String out_trade_no;

    @Schema(title = "状态码")
    private String state;

    @Schema(title = "状态码")
    private String sign;

    @Schema(title = "回调网址")
    private String callback_url;

}

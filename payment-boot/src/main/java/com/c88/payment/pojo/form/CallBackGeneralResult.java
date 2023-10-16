package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "一般支付答覆資料表單")
public class CallBackGeneralResult {

    @JsonProperty("transactionid")
    @Schema(title = "产品生成的订单号，唯一")
    private Long transactionId;

    @JsonProperty("orderid")
    @Schema(title = "商户平台生成的订单号，唯一")
    private String orderId;

    @Schema(title = "商家提交金额，范围小数点后两位")
    private BigDecimal amount;

    @JsonProperty("real_amount")
    @Schema(title = "实际存入／扣除商户的金额，范围小数点后两位")
    private BigDecimal realAmount;

    @Schema(title = "原样返回（空字符串也必需传输）")
    private String custom;

}

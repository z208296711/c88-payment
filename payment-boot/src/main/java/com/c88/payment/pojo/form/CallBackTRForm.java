package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "TR支付回調表單")
public class CallBackTRForm {

    @Schema(title = "商户号")
    private String memberid;

    @Schema(title = "订单号")
    private String orderid;

    @Schema(title = "交易时间")
    private String datetime;

    @Schema(title = "订单金额")
    private BigDecimal amount;

    @Schema(title = "交易流水号")
    private String transaction_id;

    @Schema(title = "交易状态( 成功返回00)")
    private String returncode;

    @Schema(title = "商户附加数据")
    private String attach;

}

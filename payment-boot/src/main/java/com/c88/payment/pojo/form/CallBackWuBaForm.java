package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "TB支付回調表單")
public class CallBackWuBaForm {

    @Schema(title = "商户号")
    private String merchant;

    @Schema(title = "订单号")
    private String order_id;

    @Schema(title = "提交金额")
    private BigDecimal amount;

    @Schema(title = "支付结果", description = "5:成功 3:失败")
    private String status;

    @Schema(title = "状态码", description = "成功、拒绝")
    private String message;

    @Schema(title = "状态码", description = "Md5签名结果。")
    private String sign;

}

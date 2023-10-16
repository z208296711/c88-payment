package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "FB支付回調表單")
public class CallBackFBForm {

    @Schema(title = "状态代号", description = "正常为 0")
    private String code;

    @Schema(title = "状态解释")
    private String msg;

    @Schema(title = "商号")
    private String merchant;

    @Schema(title = "订单号")
    private String merchant_order_num;

    @Schema(title = "动作")
    private String action;

    @Schema(title = "订单回应", description = "参数加密字串")
    private String order;

}

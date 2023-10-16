package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: TODO
 * @author: marcoyang
 * @date: 2023/1/3
 **/
@Data
@Builder
@Schema(title = "通用回調表單")
public class CallBackGeneralForm {
    @Schema(title = "参数值见附件「状态码」", description = "此状态码亦代表「订单状态」")
    private Integer status;

    @Schema(title = "当状态码为「成功」时返回 JSON 字符串")
    private String result;

    @Schema(title = "32 位大写 MD5 签名值")
    private String sign;

    @Schema(title = "訂單編號 without convert")
    private String order_id;

    @Schema(title = "amount without convert")
    private BigDecimal amount;

    @Schema(title = "message without convert")
    private String message;

    @Schema(title = "merchant without convert")
    private String merchant;
}

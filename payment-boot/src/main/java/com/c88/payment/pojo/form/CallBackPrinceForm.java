package com.c88.payment.pojo.form;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "太子支付回調表單")
public class CallBackPrinceForm {

    @Schema(title = "参数值见附件「状态码」", description = "此状态码亦代表「订单状态」")
    private Integer status;

    @Schema(title = "当状态码为「成功」时返回 JSON 字符串")
    private String result;

    @Schema(title = "32 位大写 MD5 签名值")
    private String sign;

}

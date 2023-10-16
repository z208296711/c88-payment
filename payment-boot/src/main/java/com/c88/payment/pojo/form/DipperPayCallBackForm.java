package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "北斗支付回調表單")
public class DipperPayCallBackForm  {

    @Schema(title = "当状态码为「成功」时返回 JSON 字符串")
    private String tradeNo;

    @Schema(title = "上分金額")
    private BigDecimal topupAmount;

    @Schema(title = "支付状态 -2上分失敗, -1建單失敗, 0:处理中, 1:上分成功,3:无须上分, 9:API审核")
    private Integer tradeStatus;

    @Schema(title = "狀態信息")
    private String message;

    @Schema(title = "32 位大写 MD5 签名值")
    private String sign;

}

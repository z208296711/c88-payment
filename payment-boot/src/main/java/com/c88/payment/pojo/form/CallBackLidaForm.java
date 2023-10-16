package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "利達支付回調表單")
public class CallBackLidaForm {

    @Schema(title = "备注")
    private String message;

    @Schema(title = "隨機字串")
    private String merchant_id;

    @Schema(title = "訂單編號")
    private String order_no;

    @Schema(title = "系統訂單編號")
    private String sys_order_no;

    @Schema(title = "訂單金額", description = "單位:元（保留小數點后2位，沒有限制幣種）")
    private BigDecimal biz_amt;

    @Schema(title = "交易狀態", description = "processing;處理中 true: 成功 fail: 失敗")
    private String status;

    @Schema(title = "簽名", description = "所有回傳欄位加入簽名不包含sign")
    private String sign;

}

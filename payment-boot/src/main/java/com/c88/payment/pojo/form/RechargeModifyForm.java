package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "修改充值訂單")
public class RechargeModifyForm {

    @Schema(title = "訂單ID")
    private Long id;

    @Schema(title = "狀態", description = "0處理中, 1成功, 2失敗")
    private Integer status;

    private Long uid;

    @Schema(title = "備註")
    private String remark;

    @JsonIgnore
    private String tradeNo;
}

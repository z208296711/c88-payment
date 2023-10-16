package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "修改充值通道表單")
public class ModifyMerchantRechargeChannelForm {

    @NotNull(message = "充值通道ID不得為空")
    @Schema(title = "充值通道ID")
    private Integer id;

    @NotNull(message = "狀態不得為空")
    @Schema(title = "狀態", description = "0停用1啟用")
    private Integer enable;
}

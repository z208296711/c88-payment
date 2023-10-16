package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "找測試通道表單")
public class FindCheckChannelForm {

    @NotNull(message = "商戶ID不得為空")
    @Parameter(description = "商戶ID")
    @Schema(title = "商戶ID")
    private Integer merchantId;

    @Parameter(description = "支付類型ID")
    @Schema(title = "支付類型ID")
    private Integer rechargeTypeId;

    @Parameter(description = "銀行ID")
    @Schema(title = "銀行ID")
    private Integer bankId;

}

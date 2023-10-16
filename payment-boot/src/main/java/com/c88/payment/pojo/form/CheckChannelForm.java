package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "測試通道表單")
public class CheckChannelForm {

    @NotNull(message = "通道ID不得為空")
    @Schema(title = "通道ID")
    private Integer channelId;

}

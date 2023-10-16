package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "會員通道排序清單")
public class MemberChannelGroupSortForm {

    @NotNull(message = "通道群組ID不得為空")
    @Schema(title = "通道群組ID")
    private Integer id;

    @NotNull(message = "排序不得為空")
    @Schema(title = "排序")
    private Integer sort;
}

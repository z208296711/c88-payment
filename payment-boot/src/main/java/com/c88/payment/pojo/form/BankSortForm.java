package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "銀行排序清單")
public class BankSortForm {

    @NotNull(message = "ID不得為空")
    @Schema(title = "ID")
    private Integer id;

    @NotNull(message = "排序不得為空")
    @Schema(title = "排序")
    private Integer sort;

}

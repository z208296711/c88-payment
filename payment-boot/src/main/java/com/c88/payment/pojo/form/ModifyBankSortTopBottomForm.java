package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "修改平台排序表單")
public class ModifyBankSortTopBottomForm {

    @NotNull(message = "銀行ID不得為空")
    @Schema(title = "銀行ID")
    private Long id;

    @Range(min = 0, max = 1, message = "修改排序方式參數錯誤")
    @Schema(title = "修改排序方式", description = "0置頂1置底")
    private Integer sortType;

    @JsonIgnore
    private String code;

}

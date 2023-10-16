package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "修改商戶備註表單")
public class ModifyMerchantNoteForm {

    @NotNull(message = "商戶ID不得為空")
    @Schema(title = "商戶ID")
    private Integer id;

    @Schema(title = "啟用狀態",description = "0停用1啟用")
    private Integer enable;

    @Schema(title = "備註")
    private String note;

    @JsonIgnore
    private String name;
}

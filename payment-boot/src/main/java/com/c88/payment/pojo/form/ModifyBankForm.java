package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Schema(title = "修改銀行")
public class ModifyBankForm {

    @Schema(title = "銀行id", required = true)
    @NotNull
    private Integer id;

    @Size(min = 2, max = 80)
    @Schema(title = "銀行縮寫", required = true)
    private String code;

    @Size(min = 2, max = 80)
    @Schema(title = "銀行名稱", required = true)
    private String name;

    @Size(min = 2, max = 80)
    @Schema(title = "銀行英文名稱", required = true)
    private String nameEn;

    @Size(min = 2, max = 80)
    @Schema(title = "提款顯示名稱", required = true)
    private String withdrawName;

    @Size(min = 2, max = 80)
    @Schema(title = "充值顯示名稱", required = true)
    private String rechargeName;

    @NotBlank
    @Schema(title = "銀行LOGO圖", required = true)
    private String logo;

    @Schema(title = "備註")
    private String note;

}

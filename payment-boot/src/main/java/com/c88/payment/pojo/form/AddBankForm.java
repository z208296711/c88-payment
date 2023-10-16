package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
@Schema(title = "新增銀行")
public class AddBankForm {

    @Size(min = 2, max = 80)
    @Schema(title = "銀行縮寫")
    private String code;

    @Size(min = 2, max = 80)
    @Schema(title = "銀行名稱")
    private String name;

    @Size(min = 2, max = 80)
    @Schema(title = "銀行英文名稱")
    private String nameEn;

    @Size(min = 2, max = 80)
    @Schema(title = "提款顯示名稱")
    private String withdrawName;

    @Size(min = 2, max = 80)
    @Schema(title = "充值顯示名稱")
    private String rechargeName;

    @NotBlank
    @Schema(title = "銀行LOGO圖")
    private String logo;

    @Schema(title = "備註")
    @Size(max = 200)
    private String note;

}

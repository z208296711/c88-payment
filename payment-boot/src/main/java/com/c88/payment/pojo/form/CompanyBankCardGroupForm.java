package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(title = "自營卡群組管理")
public class CompanyBankCardGroupForm {

    @Schema(title = "id", description = "更新時需要id")
    private Long id;

    @Schema(title = "群組名稱", required = true)
    @Size(max = 32)
    @NotBlank
    private String name;

    @Schema(title = "群組說明")
    @Size(max = 50)
    private String note;

    @JsonIgnore
    private String beforeName;

}

package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@Schema(title = "出款操作")
public class RemitForm {
    @Schema(title = "提款申請ID")
    Long id;
    @Size(max = 200)
    @Schema(title = "備註")
    String note;

    @Schema(title = "自營卡id")
    private Long companyBankCardId;

}

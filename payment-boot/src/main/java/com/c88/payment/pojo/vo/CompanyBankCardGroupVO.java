package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "自營卡群組管理")
public class CompanyBankCardGroupVO {

    @Schema(title = "id")
    private Long id;

    @Schema(title = "群組名稱")
    private String name;

    @Schema(title = "群組說明")
    private String note;

    @Schema(title = "自營卡數量")
    private Integer count;

}

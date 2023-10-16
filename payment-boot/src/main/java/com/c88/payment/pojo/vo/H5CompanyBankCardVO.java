package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "自營卡轉卡")
public class H5CompanyBankCardVO {

    @Schema(title = "唯一值")
    private Integer id;

    @Schema(title = "單筆存款上限")
    private BigDecimal maxAmount;

    @Schema(title = "單筆存款下限")
    private BigDecimal minAmount;

}

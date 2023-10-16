package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Schema(title = "自營卡轉卡-銀行卡管理")
public class CompanyBankCardForm {

    @Schema(title = "id")
    private Long id;

    @Schema(title = "代號")
    @Size(max = 20)
    @NotBlank
    private String code;

    @Schema(title = "收款人名稱")
    @Size(max = 40)
    @NotBlank
    private String owner;

    @Schema(title = "銀行別")
    private Integer bankId;

    @Schema(title = "銀行卡號")
    @Size(max = 19)
    @NotBlank
    private String bankCardNo;

    @Schema(title = "代號")
    private Integer groupId;

    @Schema(title = "單日存款上線總額")
    @Digits(integer = 10, fraction = 0)
    @DecimalMin(value = "0")
    private BigDecimal dailyMaxAmount;

    @Schema(title = "單筆存款上限")
    @Digits(integer = 10, fraction = 0)
    @DecimalMin(value = "0")
    private BigDecimal maxAmount;

    @Schema(title = "單筆存款下限")
    @Digits(integer = 10, fraction = 0)
    @DecimalMin(value = "0")
    private BigDecimal minAmount;

    @Schema(title = "轉帳說明")
    @Size(max = 500)
    private String note;

    @Schema(title = "是否產生附言 0:不產生, 1:產生")
    private Integer comments;

    @Schema(title = "啟用狀態 0:停用, 1:啟用")
    private Integer enable;

    @JsonIgnore
    private boolean isEnableChanged = false;

}

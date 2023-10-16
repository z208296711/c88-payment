package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "CheckAffiliateBalanceForm")
public class CheckAffiliateBalanceForm {

    @Schema(title = "提款密碼")
    private String withdrawPassword;

}

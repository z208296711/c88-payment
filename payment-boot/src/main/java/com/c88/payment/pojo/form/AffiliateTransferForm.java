package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "提款申請")
public class AffiliateTransferForm {

    @Schema(title = "團隊代理")
    String username;

    @Schema(title = "提款金額")
    BigDecimal amount;

    @Schema(title = "提款密碼")
    String withdrawPassword;

    @Schema(title = "備註")
    String note;

}

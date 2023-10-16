package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyWithdrawFrom {
    @Schema(title = "提款金額")
    BigDecimal amount;
    @Schema(title = "提款密碼")
    String withdrawPassword;
    @Schema(title = "會員銀行卡ID")
    Integer memberBankId;
    @Schema(title = "會員銀行卡號")
    String memberBankCardNo;
    @Schema(title = "提幣名稱")
    String cryptoName;
    @Schema(title = "提幣地址")
    String cryptoAddress;
}

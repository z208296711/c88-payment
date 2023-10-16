package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(title = "提款申請")
public class AffiliateWithdrawApplyForm {

    @Schema(title = "提款金額")
    BigDecimal amount;

    @Schema(title = "提款密碼")
    String withdrawPassword;

    @Schema(title = "會員銀行卡ID")
    @NotNull
    Integer bankId;

    @Schema(title = "會員銀行卡號")
    @NotBlank
    String bankCardNo;

    @Schema(title = "提幣名稱")
    String cryptoName;

    @Schema(title = "提幣地址")
    String cryptoAddress;

}

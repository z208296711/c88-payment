package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(title = "會員充值資訊")
public class MemberRechargeInfo {

    @Schema(title = "公司自營卡")
    private H5CompanyBankCardVO companyBankCard;

    @Schema(title = "銀行清單")
    private List<H5BankVO> banks;

}

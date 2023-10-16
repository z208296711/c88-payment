package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "前台提款頁")
public class H5MemberWithdrawVO {

    private BigDecimal balance;

    private BigDecimal withdrawLimit;

    private List<H5MemberBankVO> memberBank;

    private List<H5memberCryptoVO> memberCrypto;

}

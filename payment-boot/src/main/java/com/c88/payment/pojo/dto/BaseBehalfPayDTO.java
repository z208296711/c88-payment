package com.c88.payment.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseBehalfPayDTO {

    /**
     * 會員ID
     */
    private Long memberId;

    /**
     * 訂單ID
     */
    private String orderId;

    /**
     * 交易金額
     */
    private BigDecimal amount;

    /**
     * 收款人uid
     */
    private Long uid;

    /**
     * 收款人开户姓名
     */
    private String bankAccount;

    /**
     * 收款人银行名稱
     */
    private String bankName;

    /**
     * 收款人银行帐号
     */
    private String bankNo;

    /**
     * 银行编号
     * 参数值见附件「银行编号」
     */
    private String bankCode;

    /**
     * 开户行所在省份
     */
    private String bankProvince;

    /**
     * 开户行所在城市
     */
    private String bankCity;

    /**
     * 开户支行
     */
    private String bankSub;

    /**
     * 交易當下Ip
     */
    private String userIp;

}

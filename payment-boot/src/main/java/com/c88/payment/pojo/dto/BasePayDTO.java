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
public class BasePayDTO {

    /**
     * 會員帳號
     */
    private String memberUsername;

    /**
     * 會員Id
     */
    private Long memberId;

    /**
     * 會員手機號
     */
    private String memberPhone;

    /**
     * 訂單ID
     */
    private String orderId;

    /**
     * 三方通道ID
     */
    private String channelId;

    /**
     * 交易金額
     */
    private BigDecimal amount;

    /**
     * 支付方式銀行代碼
     */
    private String rechargeBankCode;

    /**
     * 交易當下Ip
     */
    private String userIp;

}

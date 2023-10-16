package com.c88.payment.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代理轉帳紀錄
 *
 * @TableName payment_affiliate_transfer_record
 */
@Data
public class AdminAffiliateMemberTransferRecordVO {


    /**
     * 流水號
     */
    @Schema(title = "流水號")
    private String serialNo;

    /**
     * 代理帳號
     */
    @Schema(title = "代理帳號")
    private String affiliateUsername;

    /**
     * 玩家帳號
     */
    @Schema(title = "玩家帳號Id")
    private Long memberId;


    /**
     * 玩家帳號
     */
    @Schema(title = "玩家帳號")
    private String memberUsername;

    /**
     * 轉帳金額
     */
    @Schema(title = "轉帳金額")
    private BigDecimal amount;


    /**
     * 創建時間
     */
    @Schema(title = "gmt_create")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;
}
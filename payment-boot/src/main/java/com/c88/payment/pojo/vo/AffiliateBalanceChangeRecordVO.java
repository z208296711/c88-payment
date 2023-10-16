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
public class AffiliateBalanceChangeRecordVO {
    /**
     * 流水號
     */
    @Schema(title = "流水號")
    private String serialNo;

    /**
     * 創建時間
     */
    @Schema(title = "gmt_create")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;

    /**
     * 帳變類型
     */
    @Schema(title = "帳變類型 0:提款 1:代理轉帳 2:玩家轉帳 3:微調 4:返佣")
    private Integer type;

    /**
     * 轉帳金額
     */
    @Schema(title = "轉帳金額")
    private BigDecimal amount;


    @Schema(title = "修改後金額")
    private String afterBalance;


}
package com.c88.payment.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
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
public class AffiliateTransferRecordVO {


    /**
     * 流水號
     */
    @Schema(title = "流水號")
    private String serialNo;

    /**
     * 代理Id
     */
    @Schema(title = "代理Id")
    private Long affiliateId;

    /**
     * 代理帳號
     */
    @Schema(title = "代理帳號")
    private String affiliateUsername;

    /**
     * 金額
     */
    @Schema(title = "金額")
    private BigDecimal amount;

    /**
     * 備註
     */
    @Schema(title = "備註")
    private String note;



    /**
     * 創建時間
     */
    @Schema(title = "gmt_create")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;
}
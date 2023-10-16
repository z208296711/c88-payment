package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.c88.common.core.base.BaseEntity;
import lombok.Data;

/**
 * 代理轉帳紀錄
 * @TableName payment_affiliate_transfer_record
 */
@TableName(value ="payment_affiliate_transfer_record")
@Data
public class AffiliateTransferRecord extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 流水號
     */
    @TableField(value = "serial_no")
    private String serialNo;

    /**
     * 代理Id
     */
    @TableField(value = "affiliate_id")
    private Long affiliateId;

    /**
     * 代理帳號
     */
    @TableField(value = "affiliate_username")
    private String affiliateUsername;

    /**
     * 被轉目標-代理Id
     */
    @TableField(value = "target_affiliate_id")
    private Long targetAffiliateId;

    /**
     * 被轉目標-代理帳號
     */
    @TableField(value = "target_affiliate_username")
    private String targetAffiliateUsername;

    /**
     * 金額
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 備註
     */
    @TableField(value = "note")
    private String note;

    /**
     * 更新時間
     */
    @TableField(value = "gmt_modified")
    private LocalDateTime gmtModified;

    /**
     * 創建時間
     */
    @TableField(value = "gmt_create")
    private LocalDateTime gmtCreate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
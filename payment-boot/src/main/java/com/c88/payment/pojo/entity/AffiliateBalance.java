package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName payment_affiliate_balance
 */
@TableName(value ="payment_affiliate_balance")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AffiliateBalance implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 代理ID
     */
    @TableField(value = "affiliate_id")
    private Long affiliateId;

    /**
     * 代理帳號
     */
    @TableField(value = "username")
    private String username;

    /**
     * 金額
     */
    @TableField(value = "balance")
    private BigDecimal balance;

    /**
     * 版本號
     */
    @TableField(value = "version")
    @Version
    private Integer version;

    /**
     * 最後更新時間
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
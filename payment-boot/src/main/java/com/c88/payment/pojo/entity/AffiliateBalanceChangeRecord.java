package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.c88.common.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代理資金流動紀錄
 * @TableName payment_affiliate_balance_change_record
 */
@TableName(value ="payment_affiliate_balance_change_record")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AffiliateBalanceChangeRecord extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Object id;

    /**
     * 會員Id
     */
    @TableField(value = "affiliate_id")
    private Long affiliateId;

    /**
     * 帳號
     */
    @TableField(value = "affiliate_username")
    private String affiliateUsername;

    /**
     * 帳變類型 0:提款 1:代理轉帳 2:玩家轉帳 3:微調 4:返佣
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 訂單號
     */
    @TableField(value = "serial_no")
    private String serialNo;

    /**
     * 帳變金額
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 異動前金額
     */
    @TableField(value = "before_balance")
    private BigDecimal beforeBalance;

    /**
     * 異動後金額
     */
    @TableField(value = "after_balance")
    private BigDecimal afterBalance;

    /**
     * 備註
     */
    @TableField(value = "note")
    private String note;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
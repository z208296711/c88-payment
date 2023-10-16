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
 * 代理-會員轉帳紀錄
 * @TableName payment_affiliate_member_transfer_record
 */
@TableName(value ="payment_affiliate_member_transfer_record")
@Data
public class AffiliateMemberTransferRecord extends BaseEntity {
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
     * 轉出代理Id
     */
    @TableField(value = "affiliate_id")
    private Long affiliateId;

    /**
     * 轉出代理帳號
     */
    @TableField(value = "affiliate_username")
    private String affiliateUsername;

    /**
     * 被轉入的會員Id
     */
    @TableField(value = "member_id")
    private Long memberId;

    /**
     * 被轉入的會員帳號
     */
    @TableField(value = "member_username")
    private String memberUsername;

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


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
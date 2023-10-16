package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代理銀行卡表
 * @TableName aff_affiliate_bank_card
 */
@TableName(value ="payment_affiliate_bank_card")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AffiliateBankCard extends BaseEntity {
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
     * 銀行ID
     */
    @TableField(value = "bank_id")
    private Long bankId;

    /**
     * 真實名
     */
    @TableField(value = "real_name")
    private String realName;

    /**
     * 卡號
     */
    @TableField(value = "card_no")
    private String cardNo;

    /**
     * 啟用狀態 0停用1啟用
     */
    @TableField(value = "enable")
    private Integer enable;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
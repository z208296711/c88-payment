package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 第三方支付廠商充值類型關聯
 *
 * @TableName payment_merchant_recharge_type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_merchant_recharge_type")
public class MerchantRechargeType implements Serializable {
    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 廠商Id
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 充值類型Id
     */
    @TableField(value = "recharge_type_id")
    private Long rechargeTypeId;

    @TableField(value = "recharge_type_name")
    private String rechargeTypeName;

    /**
     * 是否提供 銀行卡支付方式
     */
    @TableField(value = "is_bank")
    private Integer isBank;

    /**
     * 手續費
     */
    @TableField(value = "fee_rate")
    private BigDecimal feeRate;

    /**
     * 最低存款金額
     */
    @TableField(value = "min_amount")
    private BigDecimal minAmount;

    /**
     * 最高存款金額
     */
    @TableField(value = "max_amount")
    private BigDecimal maxAmount;

    /**
     * 備註
     */
    @TableField(value = "note")
    private String note;

    /**
     * 開關 0:關, 1:開
     */
    @TableField(value = "enable")
    private Integer enable;

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

}
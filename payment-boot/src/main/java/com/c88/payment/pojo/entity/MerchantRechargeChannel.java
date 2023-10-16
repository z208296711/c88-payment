package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 第三方支付廠商充值通道
 *
 * @TableName payment_merchant_recharge_channel
 */
@Data
@TableName(value = "payment_merchant_recharge_channel")
public class MerchantRechargeChannel extends BaseEntity implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 廠商Code
     */
    @TableField(value = "merchant_id")
    private Integer merchantId;

    /**
     * 支付方式ID
     */
    @TableField(value = "recharge_type_id")
    private Integer rechargeTypeId;

    /**
     * 銀行ID
     */
    @TableField(value = "bank_id")
    private Long bankId;

    /**
     * 支付方式銀行代碼
     */
    @TableField(value = "recharge_bank_code")
    private String rechargeBankCode;

    /**
     *
     */
    @TableField(value = "param")
    private String param;

    /**
     * 名稱
     */
    @TableField(value = "name")
    private String name;

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
     * 單日存款金額
     */
    @TableField(value = "daily_max_amount")
    private BigDecimal dailyMaxAmount;

    /**
     * 開關 0:關, 1:開
     */
    @TableField(value = "enable")
    private Integer enable;

}
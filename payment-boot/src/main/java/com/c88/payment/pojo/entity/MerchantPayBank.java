package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.c88.common.core.base.BaseEntity;
import lombok.Data;

/**
 * 第三方廠商代付銀行
 * @TableName payment_merchant_pay_bank
 */
@TableName(value ="payment_merchant_pay_bank")
@Data
public class MerchantPayBank extends BaseEntity implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 三方支付ID
     */
    @TableField(value = "merchant_pay_id")
    private Integer merchantPayId;

    /**
     * 銀行ID
     */
    @TableField(value = "bank_id")
    private Integer bankId;

    /**
     * 參數
     */
    @TableField(value = "param")
    private String param;

}
package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import com.c88.common.mybatis.handler.ListIntegerTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 第三方廠商代付管理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_merchant_pay", autoResultMap = true)
public class MerchantPay extends BaseEntity implements Serializable {
    /**
     * 三方支付ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 三方支付英文代号
     */
    @TableField(value = "merchant_code")
    private String merchantCode;

    /**
     * 餘額下限水位
     */
    @TableField(value = "threshold_balance")
    private BigDecimal thresholdBalance;

    /**
     * 處理中餘額
     */
    @TableField(value = "process_balance")
    private BigDecimal processBalance;

    /**
     * 單筆上限金額
     */
    @TableField(value = "max_amount")
    private BigDecimal maxAmount;

    /**
     * 單筆下限金額
     */
    @TableField(value = "min_amount")
    private BigDecimal minAmount;

    /**
     * 啟用開關 0:關 1:開
     */
    @TableField(value = "agent_withdraw_enable")
    private Integer agentWithdrawEnable;

    /**
     * 可開放的會員等級
     */
    @TableField(value = "vip_ids", typeHandler = ListIntegerTypeHandler.class)
    private List<Integer> vipIds;

    /**
     * 限定可用的會員標籤
     */
    @TableField(value = "tag_ids", typeHandler = ListIntegerTypeHandler.class)
    private List<Integer> tagIds;

    /**
     * 啟用開關 0:關 1:開
     */
    @TableField(value = "enable")
    private Integer enable;

    /**
     * 備註
     */
    @TableField(value = "note")
    private String note;

}
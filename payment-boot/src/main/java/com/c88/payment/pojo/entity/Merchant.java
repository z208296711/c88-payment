package com.c88.payment.pojo.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.c88.common.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 第三方支付廠商
 *
 * @TableName payment_merchant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_merchant", autoResultMap = true)
public class Merchant extends BaseEntity implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 三方支付名称
     */
    private String name;

    /**
     * 英文代号
     */
    private String code;

    /**
     * logo
     */
    private String logo;

    /**
     * 支付請求接口
     */
    private String payUrl;

    /**
     * 回調通知接口
     */
    private String notify;

    /**
     * 代付回調接口
     */
    private String behalfNotify;

    /**
     * 是否提供支付服務
     */
    private Integer isBank;

    /**
     * 是否提供支付服務
     */
    private Integer isDeposit;

    /**
     * 是否提供代付
     */
    private Boolean isWithdraw;

    /**
     * API參數
     */
    @TableField(value = "api_parameter", typeHandler = FastjsonTypeHandler.class)
    private JSONObject apiParameter;

    /**
     * ext_field
     */
    private String extField;

    /**
     * 備註
     */
    private String note;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 啟用 0:關 1:開
     */
    private Integer enable;
}
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
 * 首存訊息
 * @TableName payment_first_recharge_report
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value ="payment_member_first_recharge_report")
public class MemberFirstRechargeReport extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 存款提交日期
     */
    @TableField(value = "recharge_time")
    private LocalDateTime rechargeTime;

    /**
     * 訂單編號
     */
    @TableField(value = "order_no")
    private String orderNo;

    /**
     * 帳號
     */
    @TableField(value = "username")
    private String username;

    /**
     * 上級代理
     */
    @TableField(value = "parent_username")
    private String parentUsername;

    /**
     * 姓名
     */
    @TableField(value = "real_name")
    private String realName;

    /**
     * 首存金額
     */
    @TableField(value = "first_recharge_amount")
    private BigDecimal firstRechargeAmount;

    /**
     * 存款方式
     */
    @TableField(value = "recharge_type")
    private String rechargeType;

    /**
     * 上分到帳時間
     */
    @TableField(value = "real_time")
    private LocalDateTime realTime;

}
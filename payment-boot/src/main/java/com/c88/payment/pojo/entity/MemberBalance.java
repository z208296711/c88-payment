package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.c88.common.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @TableName payment_member_balance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_member_balance")
public class MemberBalance extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 會員ID
     */
    @TableField(value = "member_id")
    private Long memberId;

    /**
     * 會員帳號
     */
    @TableField(value = "username")
    private String username;

    /**
     * 金額
     */
    @TableField(value = "balance")
    private BigDecimal balance;

    /**
     * 首次充值時間
     */
    @TableField(value = "first_recharge_time")
    private LocalDateTime firstRechargeTime;

    /**
     * 首次充值金額
     */
    @TableField(value = "first_recharge_balance")
    private BigDecimal firstRechargeBalance;

    /**
     * 最後充值時間
     */
    @TableField(value = "last_recharge_time")
    private LocalDateTime lastRechargeTime;

    /**
     * 首次提款時間
     */
    @TableField(value = "first_withdraw_time")
    private LocalDateTime firstWithdrawTime;

    /**
     * 首次提款時間
     */
    @TableField(value = "first_withdraw_balance")
    private BigDecimal firstWithdrawBalance;

    /**
     * 最後提款時間
     */
    @TableField(value = "last_withdraw_time")
    private LocalDateTime lastWithdrawTime;

    /**
     * 版本號
     */
    @Version
    @TableField(value = "version")
    private Integer version;

}
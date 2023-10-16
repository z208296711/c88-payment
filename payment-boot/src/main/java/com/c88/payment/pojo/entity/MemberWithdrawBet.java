package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.c88.common.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName payment_member_withdraw_bet
 */
@TableName(value ="payment_member_withdraw_bet")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberWithdrawBet extends BaseEntity {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Object id;

    /**
     * 會員Id
     */
    @TableField(value = "member_id")
    private Long memberId;

    /**
     * 會員帳號
     */
    @TableField(value = "username")
    private String username;

    /**
     * 距離提現所累積的有效流水，並非帳號至今為止的有效流水
     */
    @TableField(value = "valid_bet")
    private BigDecimal validBet;

    /**
     * 當前累計要求流水+此筆帳變的要求流水
     */
    @TableField(value = "acc_bet")
    private BigDecimal accBet;

    /**
     * 版本號
     */
    @TableField(value = "version")
    @Version
    private Integer version;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
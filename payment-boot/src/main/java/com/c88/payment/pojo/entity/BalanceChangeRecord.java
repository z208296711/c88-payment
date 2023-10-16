package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 資金流動紀錄
 * @TableName payment_balance_change_record
 */
@TableName(value ="payment_balance_change_record")
@Data
public class BalanceChangeRecord extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 金額
     */
    @TableField(value = "member_id")
    private Long memberId;

    /**
     * 帳號
     */
    @TableField(value = "username")
    private String username;

    /**
     * 帳變類型  1:充值 2:提款 3:紅利：包含項目像是紅包、禮金、免費籌碼以及返水等等（不含存送優惠）4:清零：會員餘額低於某指定金額則清空該會員距離提現的總要求流水為0 5:轉帳：三方與平台間的金額移轉動作 6:佣金：代理佣金轉移 7:調整：手動調整累積要求流水 8:存送優惠
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 擴展信息
     */
    @TableField(value = "note")
    private String note;

    /**
     * 變動金額
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 變動前餘額
     */
    @TableField(value = "before_balance")
    private BigDecimal beforeBalance;

    /**
     * 當前可用餘額
     */
    @TableField(value = "current_balance")
    private BigDecimal currentBalance;

    /**
     * 打碼要求倍數
     */
    @TableField(value = "bet_rate")
    private BigDecimal betRate;

    /**
     * 要求流水
     */
    @TableField(value = "need_bet")
    private BigDecimal needBet;

    /**
     * 存送溢出要求流水
     */
    @TableField(value = "recharge_award_full_out")
    private BigDecimal rechargeAwardFullOut;

    /**
     * 有效流水: 距離提現所累積的有效流水，並非帳號至今為止的有效流水
     */
    @TableField(value = "valid_bet")
    private BigDecimal validBet;

    /**
     * 累計要求流水: 當前累計要求流水+此筆帳變的要求流水
     */
    @TableField(value = "acc_bet")
    private BigDecimal accBet;


}
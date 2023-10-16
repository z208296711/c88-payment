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

import java.io.Serializable;

/**
 * 會員銀行
 *
 * @TableName payment_member_bank
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_member_bank")
public class MemberBank extends BaseEntity implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 會員ID
     */
    @TableField(value = "member_id")
    private Integer memberId;

    /**
     * 銀行ID
     */
    @TableField(value = "bank_id")
    private Integer bankId;

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

}
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
 * 會員虛擬銀行
 *
 * @TableName payment_member_virtual_currency
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_member_crypto")
public class MemberCrypto extends BaseEntity implements Serializable {
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
     * 別名
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 協議
     */
    @TableField(value = "protocol")
    private String protocol;

    /**
     * 提幣地址
     */
    @TableField(value = "address")
    private String address;

    /**
     * 啟用狀態 0停用1啟用
     */
    @TableField(value = "enable")
    private Integer enable;

}
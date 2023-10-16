package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 會員通道VIP配置关联表
 * @TableName payment_member_channel_vip_config_mapping
 */
@TableName(value ="payment_member_channel_vip_config_mapping")
@Data
public class MemberChannelVipConfigMapping implements Serializable {
    /**
     * 會員通道ID
     */
    @TableField(value = "member_channel_id")
    private Long memberChannelId;

    /**
     * vipID
     */
    @TableField(value = "vip_config_id")
    private Long vipConfigId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
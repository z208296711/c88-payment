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

import java.time.LocalDateTime;

/**
 * 通道群組設定
 *
 * @TableName payment_member_channel_group
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_member_channel_group")
public class MemberChannelGroup extends BaseEntity {
    /**
     * 通道id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * VIP配置 ID
     */
    @TableField(value = "vip_config_id")
    private Long vipConfigId;

    /**
     * 會員通道 ID
     */
    @TableField(value = "member_chanel_id")
    private Long memberChanelId;

    /**
     * 支付類型 ID
     */
    @TableField(value = "recharge_type_id")
    private Integer rechargeTypeId;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;


}
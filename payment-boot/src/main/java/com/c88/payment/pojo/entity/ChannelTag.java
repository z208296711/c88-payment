package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 通道和標籤關聯表
 * @TableName channel_tag
 */
@TableName(value ="channel_tag")
@Data
@AllArgsConstructor
public class ChannelTag {

    @TableField(value = "channel_id")
    private Long channelId;

    /**
     * 標籤ID
     */
    @TableField(value = "tag_id")
    private Long tagId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
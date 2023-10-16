package com.c88.payment.pojo.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.c88.payment.pojo.vo.VipConfigSettingNoteVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 會員通道VIP配置設定
 *
 * @TableName payment_member_channel_vip_config_setting
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_member_channel_vip_config_setting", autoResultMap = true)
public class MemberChannelVipConfigSetting implements Serializable {

    @TableId(value = "vip_config_id")
    private Integer vipConfigId;

    @TableField(value = "label", typeHandler = FastjsonTypeHandler.class)
    private JSONArray label;

    @TableField(value = "note", typeHandler = FastjsonTypeHandler.class)
    private JSONArray note = JSON.parseArray(JSON.toJSONString(List.of(new VipConfigSettingNoteVO(""), new VipConfigSettingNoteVO(""), new VipConfigSettingNoteVO(""), new VipConfigSettingNoteVO(""), new VipConfigSettingNoteVO(""))));

    @TableField(value = "sort", typeHandler = FastjsonTypeHandler.class)
    private JSONArray sort;
}

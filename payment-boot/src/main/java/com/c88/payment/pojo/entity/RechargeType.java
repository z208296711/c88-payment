package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(title = "支付方式")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_recharge_type", autoResultMap = true)
public class RechargeType extends BaseEntity {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 名稱
     */
    @TableField(value = "name")
    private String name;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 類型 0:一般, 充值銀行:1, 自營卡:2
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 開關 0:關, 1:開
     */
    @TableField(value = "enable")
    private Integer enable;

}

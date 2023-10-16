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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 紅利紀錄表
 *
 * @TableName payment_bonus_record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_member_bonus_record")
public class MemberBonusRecord extends BaseEntity {
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
     * 上級代理帳號
     */
    @TableField(value = "parent_username")
    private String parentUsername;

    /**
     * 活動名稱
     */
    @TableField(value = "name")
    private String name;

    /**
     * 金額
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 流水倍率
     */
    @TableField(value = "bet_rate")
    private BigDecimal betRate;

    /**
     * 流水
     */
    @TableField(value = "bet")
    private BigDecimal bet;

    /**
     * 類型 0:存送優惠, 1:轉盤, 3:白菜紅包, 4:生日禮金, 5:晉級禮金, 6:免費籌碼, 7:返水
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 審核人
     */
    @Schema(title = "review_username")
    private String reviewUsername;

    /**
     * 發放時間
     */
    @TableField(value = "receive_time")
    private LocalDateTime receiveTime;

    /**
     * 結算時vip等級
     */
    @TableField(value = "receive_vip_level_name")
    private String receiveVipLevelName;

    /**
     * 備註
     */
    @TableField(value = "note")
    private String note;

}
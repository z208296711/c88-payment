package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@TableName(value = "payment_member_recharge")
@Schema(title = "充值單")
@AllArgsConstructor
@NoArgsConstructor
public class MemberRecharge extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(title = "id")
    private Long id;

    @TableField(value = "trade_no")
    @Schema(title = "內部單號")
    private String tradeNo;

    @TableField(value = "out_trade_no")
    @Schema(title = "外部單號")
    private String outTradeNo;

    @TableField(value = "username")
    @Schema(title = "會員帳號")
    private String username;

    @TableField(value = "real_name")
    @Schema(title = "真實姓名")
    private String realName;

    @TableField(value = "member_id")
    @Schema(title = "會員ID")
    private Long memberId;

    @TableField(value = "member_level_name")
    @Schema(title = "會員等級名稱")
    private String memberLevelName;

    @TableField(value = "amount")
    @Schema(title = "充值金額")
    private BigDecimal amount;

    @TableField(value = "recharge_award_id")
    @Schema(title = "'存送優惠ID'")
    private Long rechargeAwardId;

    @TableField(value = "recharge_award_name")
    @Schema(title = "存送優惠名稱")
    private String rechargeAwardName;

    @TableField(value = "recharge_award_bet_rate")
    @Schema(title = "存送優惠打碼倍率")
    private BigDecimal rechargeAwardBetRate;

    @TableField(value = "recharge_award_amount")
    @Schema(title = "存送優惠獲得金額")
    private BigDecimal rechargeAwardAmount;

    @Schema(title = "存送優惠獲得金額")
    @TableField(value = "max_award_amount")
    private BigDecimal maxAwardAmount;

    @TableField(value = "fee")
    @Schema(title = "手續費")
    private BigDecimal fee;

    @TableField(value = "real_amount")
    @Schema(title = "實際到帳金額")
    private BigDecimal realAmount;

    @TableField(value = "order_amount")
    @Schema(title = "三方訂單到帳金額")
    private BigDecimal orderAmount;

    @TableField(value = "order_real_amount")
    @Schema(title = "三方訂單實際到帳金額")
    private BigDecimal orderRealAmount;

    @TableField(value = "recharge_type_id")
    @Schema(title = "支付方式ID")
    private Integer rechargeTypeId;

    @TableField(value = "bank_card_code")
    @Schema(title = "自營卡代號")
    private String bankCardCode;

    @TableField(value = "bank")
    @Schema(title = "充值銀行")
    private String bank;

    @TableField(value = "merchant_id")
    @Schema(title = "商戶名稱ID")
    private Long merchantId;

    @TableField(value = "member_channel_id")
    @Schema(title = "會員通道ID")
    private Long memberChannelId;

    @TableField(value = "member_channel_name")
    @Schema(title = "會員通道名稱")
    private String memberChannelName;

    @TableField(value = "notes")
    @Schema(title = "附言")
    private String notes;

    @TableField(value = "remark")
    @Schema(title = "備註")
    private String remark;

    @TableField(value = "type")
    @Schema(title = "充值單類型", description = "0在線充值, 1手動充值, 2手動補單")
    private Integer type;

    @TableField(value = "status")
    @Schema(title = "狀態", description = "0處理中, 1成功, 2失敗")
    private Integer status;

    @TableField(value = "check_user")
    @Schema(title = "操作人員")
    private String checkUser;

    @TableField(value = "success_time")
    @Schema(title = "到賬時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime successTime;

    @Schema(title = "用戶提交IP")
    @TableField(value = "action_ip")
    private String actionIp;

    @Schema(title = "回查次數")
    @TableField(value = "find_back")
    private Integer findBack;

    @TableField(value = "gmt_create")
    @Schema(title = "建立時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}

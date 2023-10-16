package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.c88.common.core.base.BaseEntity;
import com.c88.common.mybatis.handler.ListLongTypeHandler;
import com.c88.common.web.annotation.I18n;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@TableName(value = "payment_member_channel", autoResultMap = true)
@Schema(title = "會員通道")
public class MemberChannel extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(title = "通道id")
    private Long id;

    @TableField(value = "recharge_type_id")
    @Schema(title = "商戶支付方式id")
    private Integer rechargeTypeId;

    @TableField(value = "company_bank_card_group_id")
    @Schema(title = "自營卡群組id")
    private Long companyBankCardGroupId;

    @TableField(value = "recharge_show_name")
    @Schema(title = "會員支付名稱")
    @I18n("be_deposit_channel.placeholder06")
    private String rechargeShowName;

    @TableField(value = "merchant_code")
    @Schema(title = "商戶代碼")
    private String merchantCode;

    @TableField(value = "merchant_id")
    @Schema(title = "商戶名稱Id", description = "推薦卡轉卡的id為0")
    private Integer merchantId;

    @TableField(value = "merchant_name")
    @Schema(title = "商戶名稱")
    private String merchantName;

    @TableField(value = "channel_name")
    @Schema(title = "會員通道名稱")
    @I18n("be_deposit_channel.placeholder07")
    private String channelName;

    @TableField(value = "vip_ids", typeHandler = ListLongTypeHandler.class)
    @Schema(title = "指派等級")
    @I18n("be_deposit_channel.BoxInfo06")
    private List<Long> vipIds;

    @TableField(value = "member_tags", typeHandler = ListLongTypeHandler.class)
    @Schema(title = "指派標籤")
    @I18n("be_deposit_channel.BoxInfo07")
    private List<Long> memberTags;

    @TableField(value = "max_recharge_amount")
    @Schema(title = "單筆充值最大金額")
    @I18n("be_deposit_channel.BoxInfo14")
    private BigDecimal maxRechargeAmount;

    @TableField(value = "min_recharge_amount")
    @Schema(title = "單筆充值最小金額")
    @I18n("be_deposit_channel.BoxInfo13")
    private BigDecimal minRechargeAmount;

    @TableField(value = "remark")
    @Schema(title = "備註")
    @I18n("be_deposit_channel.BoxInfo10")
    private String remark;

    @TableField(value = "status")
    @Schema(title = "通道狀態", description = "0停用 1啟用")
    @I18n("be_deposit_channel.column10")
    private Integer status;

    @TableField(value = "merchant_status")
    @Schema(title = "商戶狀態", description = "0商戶停用 1商戶啟用 2支付方式停用 3所有銀行停用 4部分銀行停用 5所有銀行維護 6部分銀行維護")
    private Integer merchantStatus;

    @TableField(value = "type")
    @Schema(title = "通道類型", description = "1自營卡 2商戶(三方)")
    private Integer type;

    @TableField(value = "deleted")
    @Schema(title = "刪除標記", description = "0未刪 1已刪")
    @TableLogic
    private Integer deleted;
}

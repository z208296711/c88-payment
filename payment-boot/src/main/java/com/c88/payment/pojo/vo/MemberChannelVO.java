package com.c88.payment.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.c88.common.mybatis.handler.IntegerArrayJsonTypeHandler;
import com.c88.common.mybatis.handler.StringArrayJsonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(title = "會員通道")
public class MemberChannelVO {

    @Schema(title = "通道id")
    private Long id;

    @Schema(title = "通道類型", description = "1自營卡 2商戶(三方)")
    private Integer type;

    @Schema(title = "自營卡群組id")
    private Long companyBankCardGroupId;

    @Schema(title = "商戶支付方式")
    private String rechargeTypeName;

    @Schema(title = "商戶支付方式Id")
    private String rechargeTypeId;

    @Schema(title = "會員支付名稱")
    private String rechargeShowName;

    @Schema(title = "商戶名稱id")
    private Integer merchantId;

    @Schema(title = "商戶名稱")
    private String merchantName;

    @Schema(title = "會員通道名稱")
    private String channelName;

    @TableField(exist = false, typeHandler = StringArrayJsonTypeHandler.class)
    @Schema(title = "指派等級")
    private List<VipVO> memberVipVOs;

    @TableField(exist = false, typeHandler = StringArrayJsonTypeHandler.class)
    @Schema(title = "指派標籤")
    private List<TagVO> memberTagVOs;

    @TableField(exist = false, typeHandler = IntegerArrayJsonTypeHandler.class)
    @Schema(title = "指派標籤")
    private Integer[] memberTags;

    // @Schema(title = "指派標籤")
    // private String vipIds;

    @Schema(title = "單筆充值最大金額")
    private BigDecimal maxRechargeAmount;

    @Schema(title = "單筆充值最小金額")
    private BigDecimal minRechargeAmount;

    @Schema(title = "備註")
    private String remark;

    @Schema(title = "通道狀態")
    private Integer status;

    @Schema(title = "商戶狀態")
    private Integer merchantStatus;
}

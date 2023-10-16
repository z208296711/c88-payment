package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberChannelGroupVO {

    @Schema(title = "通道id")
    private Long id;

    @Schema(title = "通道群組id")
    private Integer channelGroupId;

    @Schema(title = "商戶支付方式")
    private String rechargeTypeName;

    @Schema(title = "商戶支付方式Id")
    private String rechargeTypeId;

    @Schema(title = "會員支付名稱")
    private String rechargeShowName;

    @Schema(title = "會員支付標籤")
    private String label;

    @Schema(title = "商戶名稱")
    private String merchantName;

    @Schema(title = "會員通道名稱")
    private String channelName;

    @Schema(title = "單筆充值最大金額")
    private BigDecimal maxRechargeAmount;

    @Schema(title = "單筆充值最小金額")
    private BigDecimal minRechargeAmount;

    @Schema(title = "通道狀態")
    private Integer status;

    @Schema(title = "商戶狀態")
    private Integer merchantStatus;

    @Schema(title = "指派方式", description = "0_輪替, 1_隨機")
    private Integer assignmentType;

    @Schema(title = "排序方式")
    private Integer sort;
}

package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "支付方式")
public class RechargeTypeVO {

    @Schema(title = "id")
    private Integer id;

    @Schema(title = "名稱")
    private String name;

    @Schema(title = "排序")
    private Integer sort;

    @Schema(title = "標籤")
    private String label;

    @Schema(title = "會員通道")
    private H5MemberChannelVO channel;

    @Schema(title = "類型", description = "0:一般, 充值銀行:1, 自營卡:2")
    private Integer type;

    @Schema(title = "會員充值資訊")
    private MemberRechargeInfo rechargeInfo;

}

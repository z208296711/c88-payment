package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "商戶通道測試")
public class CheckChannelVO {

    @Schema(title = "通道ID")
    private Long channelId;

    @Schema(title = "支付類型ID")
    private Integer rechargeTypeId;

    @Schema(title = "支付類型名稱")
    private String rechargeTypeName;

    @Schema(title = "銀行ID")
    private Long bankId;

    @Schema(title = "銀行名稱")
    private String bankName;

}

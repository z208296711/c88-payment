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
@Schema(title = "充值通道資訊VO")
public class MerchantRechargeChannelSettingVO {

    @Schema(title = "通道ID")
    private Long id;

    @Schema(title = "支付通道名稱")
    private String name;

    @Schema(title = "銀行備註")
    private String note;

    @Schema(title = "啟用狀態 0:關閉 1:啟動")
    private Integer enable;

    @Schema(title = "清單層數")
    private Integer layoutType;

}

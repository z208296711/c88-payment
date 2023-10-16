package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "充值類型資訊VO")
public class MerchantRechargeTypeSettingVO {

    @Schema(title = "支付類型ID")
    private Long id;

    @Schema(title = "支付名稱")
    private String name;

    @Schema(title = "備註")
    private String note;

    @Schema(title = "啟用狀態 0:關閉 1:啟動")
    private Integer enable;

    @Schema(title = "清單層數")
    private Integer layoutType;

    @Schema(title = "充值通道清單")
    private List<MerchantRechargeChannelSettingVO> settings;

}

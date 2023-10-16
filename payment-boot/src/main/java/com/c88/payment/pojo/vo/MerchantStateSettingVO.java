package com.c88.payment.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
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
@Schema(title = "商戶狀態清單")
public class MerchantStateSettingVO {

    @Schema(title = "商戶ID")
    private Integer id;

    @Schema(title = "商戶名稱")
    private String name;

    @Schema(title = "啟用狀態 0:關閉 1:啟動")
    private Integer enable;

    @Schema(title = "清單層數")
    private Integer layoutType;

    @ApiModelProperty("商戶群組名稱")
    private List<MerchantRechargeTypeSettingVO> settings;

}

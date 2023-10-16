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
@Schema(title = "指定VIP等級支付方式排序")
public class VipConfigSettingSortVO {

    @Schema(title = "充值類型ID")
    private Integer rechargeTypeId;

    @Schema(title = "充值類型名稱")
    private String rechargeTypeName;

    @Schema(title = "排序")
    private Integer sort;

}

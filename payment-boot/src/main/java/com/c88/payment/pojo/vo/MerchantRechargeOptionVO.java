package com.c88.payment.pojo.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(title = "MerchantRechargeOptionVO")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class MerchantRechargeOptionVO {

    @Schema(title = "value")
    private Long value;

    @Schema(title = "label")
    private String label;

    @Schema(title = "minAmount")
    private BigDecimal minAmount;

    @Schema(title = "maxAmount")
    private BigDecimal maxAmount;

    @Schema(title = "子類別")
    private List<MerchantRechargeOptionVO> children;

}

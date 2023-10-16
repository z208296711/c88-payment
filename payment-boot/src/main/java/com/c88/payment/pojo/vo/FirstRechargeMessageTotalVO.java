package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "首存總合訊息")
public class FirstRechargeMessageTotalVO {

    @Schema(title = "首存人數總計")
    private Long firstRechargePersonNumber;

    @Schema(title = "首存金額總計")
    private BigDecimal firstRechargeAmount;

}

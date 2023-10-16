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
@Schema(title = "紅利列表總表")
public class MemberBonusRecordTotalVO {

    @Schema(title = "金額")
    private BigDecimal amount;

    @Schema(title = "流水")
    private BigDecimal bet;

}

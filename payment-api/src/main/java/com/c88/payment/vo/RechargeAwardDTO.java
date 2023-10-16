package com.c88.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "存送優惠資訊")
public class RechargeAwardDTO {

    @Schema(title = "存送優惠ID")
    private Long templateId;

    @Schema(title = "存送優惠名稱")
    private String name;

    @Schema(title = "存送類型", description = "1平台 2個人")
    private Integer type;

    @Schema(title = "存送模式", description = "1比例 2固定")
    private Integer mode;

    @Schema(title = "存送優惠打碼倍率")
    private BigDecimal betRate;

    @Schema(title = "存送優惠比例")
    private BigDecimal rate;

    @Schema(title = "存送優惠固贈送定金額")
    private BigDecimal fixAmount;

    @Schema(title = "存送優惠贈送最高上限")
    private BigDecimal maxAwardAmount;

    @Schema(title = "最低參與金額")
    private BigDecimal minJoinAmount;


}

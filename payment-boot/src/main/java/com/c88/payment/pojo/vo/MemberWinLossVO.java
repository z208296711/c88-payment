package com.c88.payment.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberWinLossVO {

    @Schema(title = "日期")
    private LocalDate createDate = LocalDate.now();

    @Schema(title = "上級代理")
    private String agentName = "";

    @Schema(title = "玩家帳號")
    private String username = "";

    @Schema(title = "玩家Id")
    private Integer memberId = 0;

    @Schema(title = "存款筆數")
    private Integer depositRowNum = 0;

    @Schema(title = "存款金額")
    private BigDecimal depositAmount = BigDecimal.ZERO;;

    @Schema(title = "提款筆數")
    private Integer withdrawRowNum = 0;

    @Schema(title = "提款金額")
    private BigDecimal withdrawAmount = BigDecimal.ZERO;;

    @Schema(title = "投注金額")
    private BigDecimal betAmount = BigDecimal.ZERO;;

    @Schema(title = "存送優惠")
    private BigDecimal depositAward = BigDecimal.ZERO;;

    @Schema(title = "首存金額")
    private BigDecimal downPaymentAmount = BigDecimal.ZERO;;

    @Schema(title = "手續費")
    private BigDecimal fee = BigDecimal.ZERO;

    @Schema(title = "公司淨盈利")
    private BigDecimal netProfit = BigDecimal.ZERO;

    @Schema(title = "紅利")
    private BigDecimal bonus = BigDecimal.ZERO;

    @Schema(title = "返水")
    private BigDecimal rebate = BigDecimal.ZERO;

}

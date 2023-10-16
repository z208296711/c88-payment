package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(title = "DailyRechargeWithdrawVO")
@AllArgsConstructor
@NoArgsConstructor
public class DailyRechargeWithdrawVO {

    @Schema(title = "統計日期")
    private LocalDate date;

    @Schema(title = "充值金額")
    private BigDecimal recharge;

    @Schema(title = "提款金額")
    private BigDecimal withdraw;

    @Schema(title = "該日領取存送優惠的總金額")
    private BigDecimal rechargePromotion;

    @Schema(title = "包含項目像是紅包、禮金、免費籌碼以及返水等等（不含存送優惠）")
    private BigDecimal bonus;


    public DailyRechargeWithdrawVO(LocalDate date) {
        this.date = date;
        this.recharge = BigDecimal.ZERO;
        this.withdraw = BigDecimal.ZERO;
        this.rechargePromotion = BigDecimal.ZERO;
        this.bonus = BigDecimal.ZERO;
    }
}

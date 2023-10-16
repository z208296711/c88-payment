package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(title = "AddBalanceChangeForm")
public class AddBalanceChangeForm  {


    @Schema(title = "memberId")
    private Long memberId;

    @Schema(title = "調整金額 增加傳＋ 減少傳-")
    private BigDecimal amount;

}

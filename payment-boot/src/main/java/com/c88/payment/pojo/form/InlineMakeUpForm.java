package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(title = "新增手動充值補單訂單")
public class InlineMakeUpForm {

    @NotNull(message = "會員帳號不得為空")
    @Schema(title = "會員帳號")
    private String username;

    @NotNull(message = "自營卡不得為空")
    @Schema(title = "自營卡ID")
    private Integer companyCardId;

    @NotNull(message = "充值金額不得為空")
    @Schema(title = "充值金額")
    private BigDecimal amount;

    @Schema(title = "存送優惠ID")
    private Long rechargeAwardId;

    @Schema(title = "備註")
    private String note;

    @JsonIgnore
    private String tradeNo;
}

package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@Schema(title = "新增在線充值訂單表單")
public class AddOnlineMemberRechargeForm {

    @Schema(title = "會員通道ID")
    private Integer memberChannelId;

    @Schema(title = "銀行ID")
    private Integer bankId;

    @Schema(title = "存送優惠ID")
    private Long rechargeAwardId;

    @Schema(title = "充值金額")
    @DecimalMin(value = "1")
    @Range(min = 1, message = "充值金額錯誤")
    private BigDecimal amount;
}

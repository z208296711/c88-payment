package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@Schema(title = "新增充值訂單")
public class MemberRechargeForm {

    @Schema(title = "支付方式ID")
    private Integer rechargeTypeId;

    @Schema(title = "商戶ID")
    private Long companyCardId;

    @Schema(title = "存送優惠ID")
    private Long rechargeAwardId;

    @Schema(title = "充值金額")
    @DecimalMin(value = "1")
    @Range(min = 1, message = "充值金額錯誤")
    private BigDecimal amount;
}

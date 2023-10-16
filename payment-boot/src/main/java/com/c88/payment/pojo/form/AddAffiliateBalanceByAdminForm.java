package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Schema(title = "管理員充值表單")
public class AddAffiliateBalanceByAdminForm {

    @NotNull(message = "代理ID不得為空")
    @Schema(title = "代理ID")
    private Long id;

    @NotNull(message = "充值金額不得為空")
    @Schema(title = "充值金額")
    private BigDecimal amount;

    @Schema(title = "備註")
    @Length(max = 200,message = "上限為200字")
    private String note;

}

package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(title = "商戶支付類型修改表單")
public class ModifyMerchantRechargeType {

    @NotNull(message = "商戶支付類型ID")
    @Schema(title = "商戶支付類型ID")
    private Long id;

    @Schema(title = "啟用狀態", description = "0停用 1啟用")
    private Integer enable;

    @Schema(title = "備註")
    private String note;

    @Schema(title = "手續費")
    private BigDecimal feeRate;

    @Range(min = 1, max = 9999999, message = "最小充值金額輸入錯誤")
    @Schema(title = "最小充值金額")
    private BigDecimal minAmount;

    @Range(min = 1, max = 9999999, message = "最大充值金額輸入錯誤")
    @Schema(title = "最大充值金額")
    private BigDecimal maxAmount;

    @JsonIgnore
    private String merchantName;

    @JsonIgnore
    private Integer isBank;

    @JsonIgnore
    private String rechargeTypeName;

}

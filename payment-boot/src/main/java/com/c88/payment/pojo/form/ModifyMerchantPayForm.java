package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(title = "修改代付管理表單")
public class ModifyMerchantPayForm {

    @NotNull(message = "商戶ID")
    @Schema(title = "商戶ID")
    private Integer id;

    @Schema(title = "代理出款可用",description = "0不可用 1可用")
    private Integer agentWithdrawEnable;

    @Schema(title = "啟用狀態",description = "0停用 1啟用")
    private Integer enable;

    @Schema(title = "餘額下限水位")
    private BigDecimal thresholdBalance;

    @Schema(title = "單筆提款上限")
    private BigDecimal maxAmount;

    @Schema(title = "單筆提款下限")
    private BigDecimal minAmount;

    @Schema(title = "開放會員等級ID")
    private List<Integer> vipIds;

    @Schema(title = "限定會員標籤ID")
    private List<Integer> tagIds;

    @Schema(title = "備註")
    private String note;

    @JsonIgnore
    private String merchantName;

}

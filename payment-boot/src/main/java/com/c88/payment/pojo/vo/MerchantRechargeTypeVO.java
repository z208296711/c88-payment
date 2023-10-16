package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(title = "充值類型資訊VO")
public class MerchantRechargeTypeVO {

    @Schema(title = "ID")
    private Long id;

    @Schema(title = "充值類型Id")
    private Long rechargeTypeId;

    @Schema(title = "充值類型")
    private String name;

    @Schema(title = "備註")
    private String note;

    @Schema(title = "最低存款金額")
    private BigDecimal feeRate;

    @Schema(title = "最低存款金額")
    private BigDecimal minAmount;

    @Schema(title = "最高存款金額")
    private BigDecimal maxAmount;

    @Schema(title = "啟用狀態 0:關閉 1:啟動")
    private Integer enable;

    @Schema(title = "支援銀行")
    private List<String> supportBanks;

}

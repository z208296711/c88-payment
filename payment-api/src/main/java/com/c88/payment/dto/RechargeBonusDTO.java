package com.c88.payment.dto;

import com.c88.common.core.enums.BalanceChangeTypeLinkEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "充值紅利事件")
public class RechargeBonusDTO {

    @Schema(title = "會員ID")
    private Long memberId;

    @Schema(title = "會員帳號")
    private String username;

    @Schema(title = "優惠項目類型")
    private BalanceChangeTypeLinkEnum balanceChangeTypeLinkEnum;

    @Schema(title = "金額")
    private BigDecimal amount;

    @Schema(title = "流水倍率")
    private BigDecimal betRate;

    @Schema(title = "審核人帳號")
    private String reviewUsername;

    @Schema(title = "備註")
    private String note;

    @Schema(title = "創建時間")
    private LocalDateTime gmtCreate;

}

package com.c88.payment.dto;

import com.c88.common.core.enums.BalanceChangeTypeLinkEnum;
import com.c88.payment.vo.RechargeAwardDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "增加餘額")
public class AddBalanceDTO implements Serializable {

    @NotNull(message = "會員ID不得為空")
    @Schema(title = "會員ID")
    private Long memberId;

    @NotNull(message = "變動金額不得為空")
    @Schema(title = "變動金額(平台分數)")
    private BigDecimal balance;

    @NotNull(message = "打碼倍率不得為空")
    @Schema(title = "打碼倍率")
    private BigDecimal betRate;

    @NotNull(message = "帳變類型不得為空")
    @Schema(title = "帳變類型", description = "參考 BalanceChangeTypeEnum")
    private Integer type;

    @Schema(title = "訂單號")
    private String orderId;

    @Schema(title = "擴展信息")
    private String note;

    @Schema(title = "優惠審核帳號")
    private String bonusReviewUsername;

    @Schema(title = "對應信息")
    private BalanceChangeTypeLinkEnum balanceChangeTypeLinkEnum;

    @Schema(title = "創建時間")
    private LocalDateTime gmtCreate;

    @Schema(title = "存送優惠相關訊息 有存送優惠就必傳 沒有就傳null")
    private RechargeAwardDTO rechargeAwardDTO;
    
}
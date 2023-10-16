package com.c88.payment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class MemberDepositDTO {

    @Schema(title = "內部單號")
    private String tradeNo;

    @Schema(title = "支付方式",description = "i18n key")
    private String rechargeType;

    @Schema(title = "會員名稱")
    private String username;

    @Schema(title = "充值金額")
    private BigDecimal amount;

    @Schema(title = "到帳金額")
    private BigDecimal realAmount;

    @Schema(title = "贈送金額")
    private BigDecimal rechargeAwardAmount;

    @Schema(title = "存款時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(title = "狀態", description = "狀態 0處理中, 1成功, 2失敗" )
    private Integer status;

}

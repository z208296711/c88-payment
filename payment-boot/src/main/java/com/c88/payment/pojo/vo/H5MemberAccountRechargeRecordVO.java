package com.c88.payment.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(title = "會員帳務紀錄")
public class H5MemberAccountRechargeRecordVO {

    @Schema(title = "單號")
    private String tradeNo;

    @Schema(title = "充值類型i18n")
    private String rechargeTypeI18N;

    @Schema(title = "狀態", description = "0處理中, 1成功, 2失敗")
    private Integer status;

    @Schema(title = "充值金額")
    private BigDecimal amount;

    @Schema(title = "實際到帳金額")
    private BigDecimal realAmount;

    @Schema(title = "手續費")
    private BigDecimal fee;

    @Schema(title = "申請時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;

}

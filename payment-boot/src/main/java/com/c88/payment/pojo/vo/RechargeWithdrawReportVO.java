package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeWithdrawReportVO {
    @Schema(title = "帳變時間")
    LocalDateTime modifyTime;

    @Schema(title = "會員帳號")
    String memberUsername;

    @Schema(title = "姓名")
    String memberRealName;

    @Schema(title = "上級代理")
    String parentUsername;

    @Schema(title = "註冊時間")
    LocalDateTime registerTime;

    @Schema(title = "登錄次數")
    Integer loginCount;

    @Schema(title = "最後登錄時間")
    LocalDateTime lastLoginTime;

    @Schema(title = "主帳戶餘額")
    BigDecimal balance;

    @Schema(title = "存款筆數")
    Integer rechargeCount;

    @Schema(title = "存款總額")
    BigDecimal rechargeTotalAmount;

    @Schema(title = "存款手續費")
    BigDecimal rechargeFee;

    @Schema(title = "取款筆數")
    Integer withdrawCount;

    @Schema(title = "取款總額")
    BigDecimal withdrawTotalAmount;

    @Schema(title = "取款手續費")
    BigDecimal withdrawFee;

    Long memberId;
}

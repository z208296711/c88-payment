package com.c88.payment.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(title = "充值單")
public class RechargeVO {

    @Schema(title = "ID")
    private Long id;

    @Schema(title = "內部單號")
    private String tradeNo;

    @Schema(title = "外部單號")
    private String outTradeNo;

    @Schema(title = "會員帳號")
    private String username;

    @Schema(title = "真實姓名")
    private String realName;

    @Schema(title = "會員ID")
    private Long memberId;

    @Schema(title = "會員等級名稱")
    private String memberLevelName;

    @Schema(title = "充值金額")
    private BigDecimal amount;

    @Schema(title = "存送優惠名稱")
    private String rechargeAwardName;

    @Schema(title = "手續費")
    private BigDecimal fee;

    @Schema(title = "實際到帳金額")
    private BigDecimal realAmount;

    @Schema(title = "支付方式")
    private String rechargeType;

    @Schema(title = "自營卡代號")
    private String bankCardCode;

    @Schema(title = "充值銀行")
    private String bank;

    @Schema(title = "商戶名稱")
    private String merchant;

    @Schema(title = "附言")
    private String notes;

    @Schema(title = "備註")
    private String remark;

    @Schema(title = "充值單類型", description = "0在線充值, 1手動充值, 2手動補單")
    private Integer type;

    @Schema(title = "status", description = "0處理中, 1成功, 2失敗")
    private Integer status;

    @Schema(title = "操作人員")
    private String checkUser;

    @Schema(title = "到賬時間", example = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime successTime;

    @Schema(title = "申請時間", example = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;
}

package com.c88.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberRechargeSuccessDTO implements Serializable {


    @Schema(title = "id")
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

    @Schema(title = "'存送優惠ID'")
    private Long rechargeAwardId;

    @Schema(title = "存送優惠名稱")
    private String rechargeAwardName;

    @Schema(title = "存送優惠打碼倍率")
    private BigDecimal rechargeAwardBetRate;

    @Schema(title = "存送優惠獲得金額")
    private BigDecimal rechargeAwardAmount;

    @Schema(title = "存送優惠獲得金額")
    private BigDecimal maxAwardAmount;

    @Schema(title = "手續費")
    private BigDecimal fee;

    @Schema(title = "實際到帳金額")
    private BigDecimal realAmount;

    @Schema(title = "三方訂單到帳金額")
    private BigDecimal orderAmount;

    @Schema(title = "三方訂單實際到帳金額")
    private BigDecimal orderRealAmount;

    @Schema(title = "支付方式ID")
    private Integer rechargeTypeId;

    @Schema(title = "支付方式萬國碼")
    private String rechargeTypeI18nKey;

    @Schema(title = "自營卡代號")
    private String bankCardCode;

    @Schema(title = "充值銀行")
    private String bank;

    @Schema(title = "商戶名稱ID")
    private Long merchantId;

    @Schema(title = "會員通道ID")
    private Long memberChannelId;

    @Schema(title = "會員通道名稱")
    private String memberChannelName;

    @Schema(title = "附言")
    private String notes;

    @Schema(title = "備註")
    private String remark;

    @Schema(title = "充值單類型", description = "0在線充值, 1手動充值, 2手動補單")
    private Integer type;

    @Schema(title = "狀態", description = "0處理中, 1成功, 2失敗")
    private Integer status;

    @Schema(title = "操作人員")
    private String checkUser;

    @Schema(title = "到賬時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime successTime;

    @Schema(title = "用戶提交IP")
    private String actionIp;

    @Schema(title = "回查次數")
    private Integer findBack;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtCreate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtModified;
}
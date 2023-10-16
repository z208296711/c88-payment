package com.c88.payment.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(title = "紅利列表總表")
public class MemberBonusRecordVO {

    @Schema(title = "會員ID")
    private Long memberId;

    @Schema(title = "會員帳號")
    private String username;

    @Schema(title = "上級代理帳號")
    private String parentUsername;

    @Schema(title = "活動名稱")
    private String name;

    @Schema(title = "金額")
    private BigDecimal amount;

    @Schema(title = "流水倍率")
    private BigDecimal betRate;

    @Schema(title = "流水")
    private BigDecimal bet;

    @Schema(title = "發放時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

    @Schema(title = "結算時vip等級名稱")
    private String receiveVipLevelName;

    @Schema(title = "審核帳號")
    private String reviewUsername;

    @Schema(title = "note")
    private String note;
}

package com.c88.payment.pojo.vo;

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
@Schema(title = "首存信息")
public class MemberFirstRechargeReportVO {

    @Schema(title = "存款提交日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rechargeTime;

    @Schema(title = "訂單編號")
    private String orderNo;

    @Schema(title = "帳號")
    private String username;

    @Schema(title = "上級代理")
    private String parentUsername;

    @Schema(title = "姓名")
    private String realName;

    @Schema(title = "首存金額")
    private BigDecimal firstRechargeAmount;

    @Schema(title = "存款方式")
    private String rechargeType;

    @Schema(title = "上分到帳時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime realTime;

}

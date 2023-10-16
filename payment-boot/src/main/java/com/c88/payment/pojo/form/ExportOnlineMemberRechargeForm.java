package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(title = "匯出在線充值訂單")
public class ExportOnlineMemberRechargeForm {

    @Schema(title = "充值單號")
    private String tradeNo;

    @Schema(title = "會員等級名稱")
    private String memberLevelName;

    @Schema(title = "狀態", description = "0處理中, 1成功, 2失敗")
    private Integer status;

    @Schema(title = "會員帳號")
    private String username;

    @Schema(title = "商戶ID")
    private Long merchantId;

    @Schema(title = "支付類型ID")
    private Long rechargeTypeId;

    @NotNull(message = "開始時間不得為空")
    @Schema(title = "開始時間", description = "pattern: yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "結束時間不得為空")
    @Schema(title = "結束時間", description = "pattern: yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotNull(message = "時間類型不得為空")
    @Schema(title = "時間類型", description = "0申請時間 1到帳時間")
    private Integer timeType;

    @NotNull(message = "時區不得為空")
    @Schema(title = "時區")
    private Integer gmtTime;

}

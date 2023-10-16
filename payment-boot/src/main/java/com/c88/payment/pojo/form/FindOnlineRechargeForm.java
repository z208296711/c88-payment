package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(title = "查詢充值訂單")
public class FindOnlineRechargeForm extends BasePageQuery {

    @Parameter(description = "充值單號")
    @Schema(title = "充值單號")
    private String tradeNo;

    @Parameter(description = "會員等級名稱")
    @Schema(title = "會員等級名稱")
    private String memberLevelName;

    @Parameter(description = "狀態 0處理中, 1成功, 2失敗")
    @Schema(title = "狀態", description = "0處理中, 1成功, 2失敗")
    private Integer status;

    @Parameter(description = "會員帳號")
    @Schema(title = "會員帳號")
    private String username;

    @Parameter(description = "商戶ID")
    @Schema(title = "商戶ID")
    private Long merchantId;

    @Parameter(description = "支付類型ID")
    @Schema(title = "支付類型ID")
    private Long rechargeTypeId;

    @Parameter(description = "開始時間")
    @Schema(title = "開始時間", description = "pattern: yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Parameter(description = "結束時間")
    @Schema(title = "結束時間", description = "pattern: yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotNull(message = "時間類型不得為空")
    @Parameter(description = "時間類型 0申請時間 1到帳時間", example = "0")
    @Schema(title = "時間類型", description = "0申請時間 1到帳時間")
    private Integer timeType;

}

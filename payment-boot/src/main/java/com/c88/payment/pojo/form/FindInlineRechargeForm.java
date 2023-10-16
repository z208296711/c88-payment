package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(title = "查詢充值訂單")
public class FindInlineRechargeForm extends BasePageQuery {

    @Parameter(description = "訂單編號")
    @Schema(title = "訂單編號")
    private String tradeNo;

    @Parameter(description = "帳號查詢")
    @Schema(title = "帳號查詢")
    private String name;

    @Parameter(description = "會員等級名稱")
    @Schema(title = "會員等級名稱")
    private String memberLevelName;

    @Parameter(description = "支付類型ID")
    @Schema(title = "支付類型ID")
    private Long rechargeTypeId;

    @Parameter(description = "商戶名稱")
    @Schema(title = "商戶名稱")
    private Long merchantId;

    @Parameter(description = "狀態 0處理中, 1成功, 2失敗")
    @Schema(title = "狀態", description = "0處理中, 1成功, 2失敗")
    private Integer status;

    @Parameter(description = "時間查詢-開始")
    @Schema(title = "時間查詢-開始", description = "pattern: yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Parameter(description = "時間查詢-結束")
    @Schema(title = "時間查詢-結束", description = "pattern: yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotNull(message = "時間類型不得為空")
    @Parameter(description = "時間查詢類型")
    @Schema(title = "時間查詢類型", description = "1申請時間 2到賬時間")
    private Integer timeType;

    @NotNull(message = "時區不得為空")
    @Schema(title = "時區")
    private Integer gmtTime;
}

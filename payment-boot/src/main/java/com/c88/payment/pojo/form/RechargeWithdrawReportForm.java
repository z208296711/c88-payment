package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(title = "查找玩家充提報表")
public class RechargeWithdrawReportForm extends BasePageQuery {

    @Schema(title = "開始時間")
    private String startTime;

    @Schema(title = "結束時間")
    private String endTime;

    @Schema(title = "時區", description = "+8:00")
    private String zone = "+8:00";

    @Schema(title = "帳號")
    private String username;
}

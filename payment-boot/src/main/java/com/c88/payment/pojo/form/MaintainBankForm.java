package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(title = "維護銀行表單")
public class MaintainBankForm {

    @NotNull(message = "銀行ID不的為空")
    @Schema(title = "銀行ID")
    private Long id;

    @NotNull(message = "每日維護開關不的為空")
    @Schema(title = "指定維護", description = "0停用1啟用", example = "0")
    private Integer assignEnable;

    @Schema(title = "指定維護開始時間", description = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignStartTime;

    @Schema(title = "指定維護結束時間", description = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignEndTime;

    @NotNull(message = "每日維護開關不的為空")
    @Schema(title = "每日維護開關", description = "0停用1啟用", example = "0")
    private Integer dailyEnable;

    @Schema(title = "每日維護開始時間", description = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime dailyStartTime;

    @Schema(title = "每日維護結束時間", description = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime dailyEndTime;

    @JsonIgnore
    private String code;

}

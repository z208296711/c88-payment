package com.c88.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "增加餘額")
public class BankDTO implements Serializable {

    @Schema(title = "ID")
    private Integer id;

    @Schema(title = "名稱")
    private String name;

    @Schema(title = "英文縮寫")
    private String code;

    @Schema(title = "英文名稱")
    private String nameEn;

    @Schema(title = "提款名稱")
    private String withdrawName;

    @Schema(title = "充值名稱")
    private String rechargeName;

    @Schema(title = "註記")
    private String note;

    @Schema(title = "銀行狀態", description = "0維護中1啟用中2排程中")
    private Integer state;

    @Schema(title = "每日維護時間", description = "0關閉1啟動")
    private Integer dailyEnable;

    @Schema(title = "指定維護時間 0關閉1啟動")
    private Integer assignEnable;

    @Schema(title = "維護開始時間")
    private LocalTime dailyStartTime;

    @Schema(title = "維護結束時間")
    private LocalTime dailyEndTime;

    @Schema(title = "指定維護開始時間")
    private LocalDateTime assignStartTime;

    @Schema(title = "指定維護結束時間")
    private LocalDateTime assignEndTime;

    @Schema(title = "銀行logo")
    private String logo;

    @Schema(title = "是否被刪除", description = "0否1是")
    private Integer deleted;

    @Schema(title = "排序")
    private Integer sort;
}
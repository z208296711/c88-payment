package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(title = "銀行表單")
public class BankVO {

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

    @Schema(title = "維護開始時間")
    private String dailyStartTime;

    @Schema(title = "維護結束時間")
    private String dailyEndTime;

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

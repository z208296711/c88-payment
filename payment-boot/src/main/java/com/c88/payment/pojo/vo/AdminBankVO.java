package com.c88.payment.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Data
@Schema(title = "銀行管理")
public class AdminBankVO {

    @Schema(title = "ID")
    private Integer id;

    @Schema(title = "銀行代號(縮寫)")
    private String code;

    @Schema(title = "圖片")
    private String logo;

    @Schema(title = "銀行名稱")
    private String name;

    @Schema(title = "銀行英文名稱")
    private String nameEn;

    @Schema(title = "提款顯示名稱")
    private String withdrawName;

    @Schema(title = "充值顯示名稱")
    private String rechargeName;

    @Schema(title = "充值使用商戶清單")
    private List<String> rechargeMerchantList = Collections.emptyList();

    @Schema(title = "備註")
    private String note;

    @Schema(title = "排序")
    private Integer sort;

    @Schema(title = "指定維護開始時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignStartTime;

    @Schema(title = "指定維護結束時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignEndTime;

    @Schema(title = "每日維護開始時間")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime dailyStartTime;

    @Schema(title = "每日維護結束時間")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime dailyEndTime;

    @Schema(title = "指定維護時間啟動狀態(0關閉1啟動)")
    private Integer assignEnable;

    @Schema(title = "每日維護時間啟動狀態(0關閉1啟動)")
    private Integer dailyEnable;

    @Schema(title = "銀行狀態(0維護中，1啟用中，2排程中)")
    private Integer state;

}

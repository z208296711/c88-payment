package com.c88.payment.pojo.vo.withdraw;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RemitReportVO {
    @Schema(title = "提款單號")
    private String withdrawNo;
    @Schema(title = "帳號")
    private String username;
    @Schema(title = "帳號類型")
    private String userType = "會員";
    @Schema(title = "提款金額")
    private BigDecimal amount;
    @Schema(title = "會員等級")
    private String vip;
    @Schema(title = "渠道")
    private String channel;
    @Schema(title = "銀行帳號/提幣地址")
    private String bankCardNo;
    @Schema(title = "開戶名")
    private String realName;
    @Schema(title = "備註")// 確認出款或取消出款等動作，提交時所填的備註訊息
    private String remitNote;
    @Schema(title = "出款狀態")
    private String remitStateStr;
    @Schema(title = "申請時間")
    private String applyTimeStr;
    @Schema(title = "審核人")
    private String approveUser;
    @Schema(title = "審核通過時間")
    private String approveTime;
    @Schema(title = "處理時間")
    private String remitTimeStr;
    @TableField(exist = false)
    @Schema(title = "處理速度")
    private String speed;
    @Schema(title = "出款方式")
    private String remitType;
    @Schema(title = "出款人")
    private String remitUser;

    @JsonIgnore
    @Schema(title = "會員等級id")
    private Integer vipId;
    @JsonIgnore
    @Schema(title = "一審時間（處置時間）")
    private LocalDateTime firstTime;
    @JsonIgnore
    @Schema(title = "二審時間（處置時間）")
    private LocalDateTime secondTime;
    @JsonIgnore
    @Schema(title = "一審人員（領取人員與處置人員相同）")
    private String firstUser;
    @JsonIgnore
    @Schema(title = "二審人員（領取人員與處置人員相同）")
    private String secondUser;
    @JsonIgnore
    @Schema(title = "出款狀態")
    private Byte remitState;
    @JsonIgnore
    @Schema(title = "申請時間")
    private LocalDateTime applyTime;
    @JsonIgnore
    @Schema(title = "處理時間")
    private LocalDateTime remitTime;

}

package com.c88.payment.pojo.vo.withdraw;

import com.baomidou.mybatisplus.annotation.TableField;
import com.c88.common.mybatis.handler.IntegerArrayJsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawReportVO {
    @Schema(title = "帳號")
    private String username;
    @Schema(title = "提款金額")
    private BigDecimal amount;
    @Schema(title = "提款單號")
    private String withdrawNo;
    @Schema(title = "會員標籤")
    private String tags;
    @TableField(exist = false)
    @Schema(title = "最後備註")// 為會員詳情的最後備註
    private String lastRemark;
    @Schema(title = "風控維度")
    private String risks;
    @Schema(title = "提款狀態", description = "0_待審核, 2_提交二審, 3_審核通過, 4_審核拒絕")
    private String stateStr;
    @Schema(title = "申請/一審/二審時間")
    private String applyTimeStr;

    @Schema(title = "一審人員")
    private String firstUser;

    @TableField(exist = false)
    @Schema(title = "一審速度")
    private String firstSpeed;

    @Schema(title = "二審人員")
    private String secondUser;

    @TableField(exist = false)
    @Schema(title = "二審速度")
    private String secondSpeed;

    @TableField(exist = false)
    @Schema(title = "出款人員")
    private String remitUser;

    @JsonIgnore
    private String riskTypes;

    @JsonIgnore
    @TableField(typeHandler = IntegerArrayJsonTypeHandler.class)
    @Schema(title = "會員標籤id", hidden = true)
    private Integer[] tagIds;

    @JsonIgnore
    @Schema(title = "提款狀態", description = "0_待審核, 2_提交二審, 3_審核通過, 4_審核拒絕")
    private Byte state;

    @JsonIgnore
    @Schema(title = "申請時間")
    private LocalDateTime applyTime;
    @JsonIgnore
    @Schema(title = "一審時間")
    private LocalDateTime firstTime;
    @JsonIgnore
    @Schema(title = "二審時間")
    private LocalDateTime secondTime;
}

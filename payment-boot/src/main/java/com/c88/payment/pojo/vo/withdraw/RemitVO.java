package com.c88.payment.pojo.vo.withdraw;

import com.baomidou.mybatisplus.annotation.TableField;
import com.c88.payment.pojo.entity.MemberWithdraw;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RemitVO extends MemberWithdraw {

    @TableField(exist = false)
    @Schema(title = "渠道")
    private String channel;
    @TableField(exist = false)
    @Schema(title = "審核人")
    private String approveUser;
    @TableField(exist = false)
    @Schema(title = "審核通過時間")
    private LocalDateTime approveTime;
    @TableField(exist = false)
    @Schema(title = "出款方式")
    private String remitType;

}

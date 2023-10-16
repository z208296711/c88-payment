package com.c88.payment.pojo.vo.withdraw;

import com.baomidou.mybatisplus.annotation.TableField;
import com.c88.payment.pojo.entity.MemberWithdraw;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class WithdrawVO extends MemberWithdraw {

    @TableField(exist = false)
    @Schema(title = "最後備註")// 為會員詳情的最後備註
    private String lastRemark;

    @TableField(exist = false)
    private String riskTypes;

    @Schema(title = "風控維度", description = "返回i18n code")
    private List<String> risks;

}

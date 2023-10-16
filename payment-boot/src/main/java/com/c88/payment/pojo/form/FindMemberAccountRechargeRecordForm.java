package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "查詢會員存款紀錄表單")
public class FindMemberAccountRechargeRecordForm extends BasePageQuery {

    @NotNull(message = "狀態不得為空")
    @Parameter(description = "狀態 0全部 1成功 2失敗 3處理中")
    @Schema(title = "狀態")
    private Integer status;

    @NotNull(message = "時間類型不得為空")
    @Parameter(description = "時間類型 1今日 2昨天 3七天內 4三十天內")
    @Schema(title = "時間類型", description = "1今日 2昨天 3七天內 4三十天內")
    private Integer timeType;

}

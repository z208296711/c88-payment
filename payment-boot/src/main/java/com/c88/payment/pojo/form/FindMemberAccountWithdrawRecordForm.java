package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "查詢帳務紀錄提款表單")
public class FindMemberAccountWithdrawRecordForm extends BasePageQuery {

    @NotNull(message = "狀態不得為空")
    @Parameter(description = "狀態 0全部 1審核中 2審核拒絕 3出款中 4成功 5失敗")
    @Schema(title = "狀態", description = "0全部 1審核中 2審核拒絕 3出款中 4成功 5失敗")
    private Integer status;

    @NotNull(message = "時間類型不得為空")
    @Parameter(description = "時間類型 1今日 2昨天 3七天內 4三十天內")
    @Schema(title = "時間類型", description = "1今日 2昨天 3七天內 4三十天內")
    private Integer timeType;

}

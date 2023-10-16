package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "銀行表單")
public class AdminSearchBankForm extends BasePageQuery {

    @Parameter(description = "直接輸入銀行縮寫精準搜尋")
    @Schema(title = "直接輸入銀行縮寫精準搜尋")
    private String code;

    @Parameter(description = "銀行狀態 0:維護中, 1:啟用, 2:排程中")
    @Schema(title = "銀行狀態 0:維護中, 1:啟用, 2:排程中")
    private Integer state;

    @Parameter(description = "查詢銀行類型 0所有銀行 1商戶充值使用銀行", example = "0", required = true)
    @Schema(title = "查詢銀行類型")
    private Integer searchBankType = 0;

}

package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "SearchRechargeWithdrawForm")
public class SearchRechargeWithdrawForm extends BasePageQuery {

    @Schema(title = "時間起")
    private String startTime;

    @Schema(title = "時間訖")
    private String endTime;

    @Schema(title = "時區", description = "+8:00")
    private String zone = "+8:00";

}

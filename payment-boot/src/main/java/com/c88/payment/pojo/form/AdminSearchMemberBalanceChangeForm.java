package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "AdminSearchMemberBalanceChangeForm")
public class AdminSearchMemberBalanceChangeForm extends BasePageQuery {


    @Schema(title = "memberId")
    private Long memberId;

    @Schema(title = "時間起")
    private String startTime;

    @Schema(title = "時間訖")
    private String endTime;

    @Parameter(description = "帳變類型")
    @Schema(title = "帳變類型")
    private List<Integer> types;

}

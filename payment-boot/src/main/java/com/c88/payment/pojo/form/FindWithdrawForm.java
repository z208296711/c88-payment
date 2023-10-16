package com.c88.payment.pojo.form;


import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(title = "提款申請列表查詢參數")
public class FindWithdrawForm extends BasePageQuery {

    @Parameter(description = "時間類型，0_申請時間, 1_一審時間, 2_二審時間")
    @Schema(title = "時間類型", description = "0_申請時間, 1_一審時間, 2_二審時間")
    private Integer timeType;

    @Parameter(description = "狀態，0_待審核, 2_提交二審, 3_審核通過, 4_審核拒絕")
    @Schema(title = "狀態", description = "0_待審核, 2_提交二審, 3_審核通過, 4_審核拒絕")
    private Integer status;

    @Parameter(description = "風控維度")
    @Schema(title = "風控維度")
    private Byte riskType;

    @Parameter(description = "提款單號")
    @Schema(title = "提款單號")
    private String withdrawNo;

    @Parameter(description = "會員帳號")
    @Schema(title = "會員帳號")
    private String username;

    @Parameter(description = "一審人員，提款一審列表查詢的處理人")
    @Schema(title = "一審人員", description = "提款一審列表查詢的處理人")
    private String firstUser;

    @Parameter(description = "二審人員，提款二審列表查詢的處理人")
    @Schema(title = "二審人員", description = "提款二審列表查詢的處理人")
    private String secondUser;

    @Parameter(description = "開始時間(格式：yyyy-mm-ss hh:mm:ss)")
    @Schema(title = "開始時間(格式：yyyy-mm-ss hh:mm:ss)")
    private String beginTime;

    @Parameter(description = "結束時間(格式：yyyy-mm-ss hh:mm:ss)")
    @Schema(title = "結束時間(格式：yyyy-mm-ss hh:mm:ss)")
    private String endTime;

    @Parameter(description = "審級，1_提款一審的列表查詢, 2_提款二審的列表查詢")
    @Schema(title = "審級", description = "1_提款一審的列表查詢, 2_提款二審的列表查詢")
    Integer level;

    @Schema(title = "當前登入的管理帳號", hidden = true)
    private String currentUser;

    @Schema(title = "時區，+X", example = "0(UTC), 8(Taipei)")
    private Integer gmtTime = 0;

    @Schema(title = "代理帳號", hidden = true)
    private String affiliateUsername;

    @Schema(title = "代理/會員")
    private Integer type;

}

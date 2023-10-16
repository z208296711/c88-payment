package com.c88.payment.pojo.form;


import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
@Schema(title = "出款處理列表查詢參數")
public class FindRemitForm extends BasePageQuery {

    @Parameter(description = "時間類型，0_申請時間, 1_處理時間")
    @Schema(title = "時間類型", description = "0_申請時間, 1_處理時間")
    private Integer timeType;

    @Parameter(description = "狀態，0_待領取, 1_人工出款中, 2_人工出款成功, 3_人工取消出款, 4_自動代付成功, 5_代付出款中, 6_自動代付失敗, 7_自動代付失敗（實名失敗）, 8_撤銷出款")
    @Schema(title = "狀態", description = "0_待領取, 1_人工出款中, 2_人工出款成功, 3_人工取消出款, 4_自動代付成功, 5_代付出款中, 6_自動代付失敗, 7_自動代付失敗（實名失敗）, 8_撤銷出款")
    private List<Integer> statuses;

    @Parameter(description = "提款單號")
    @Schema(title = "提款單號")
    private String withdrawNo;

    @Parameter(description = "出款人帳號")
    @Schema(title = "出款人帳號")
    private String remitUser;

    @Parameter(description = "商戶id")
    @Schema(title = "商戶id")
    private Long merchantId;

    @Parameter(description = "自營卡id")
    @Schema(title = "自營卡id")
    private Long companyBankCardId;

    @Parameter(description = "會員帳號")
    @Schema(title = "會員帳號")
    private String username;

    @Parameter(description = "代理帳號")
    @Schema(title = "代理帳號")
    private String affiliateUsername;

    @Parameter(description = "代理帳號")
    @Schema(title = "代理帳號")
    private String agent;

    @Parameter(description = "會員等級id")
    @Schema(title = "會員等級id")
    private Integer vipId;

    @Parameter(description = "最小金額")
    @Schema(title = "最小金額")
    private Integer minAmount;

    @Parameter(description = "最大金額")
    @Schema(title = "最大金額")
    private Integer maxAmount;

    @Parameter(description = "開始時間(格式：yyyy-mm-ss hh:mm:ss)")
    @Schema(title = "開始時間(格式：yyyy-mm-ss hh:mm:ss)")
    private String beginTime;

    @Parameter(description = "結束時間(格式：yyyy-mm-ss hh:mm:ss)")
    @Schema(title = "結束時間(格式：yyyy-mm-ss hh:mm:ss)")
    private String endTime;

    @Schema(title = "時區，+X", example = "0(UTC), 8(Taipei)")
    private Integer gmtTime = 0;

}

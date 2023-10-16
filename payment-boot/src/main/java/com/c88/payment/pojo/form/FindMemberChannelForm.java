package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "查詢會員通道")
public class FindMemberChannelForm extends BasePageQuery {

    @Schema(title = "通道id")
    private Long id;

    @Schema(title = "商戶名稱id")
    private Integer merchantId;

    @Schema(title = "商戶支付方式id")
    private Integer rechargeTypeId;

    @Schema(title = "自營卡群組id")
    private Integer companyBankCardGroupId;

    @Schema(title = "會員支付名稱")
    private String rechargeShowName;

    @Schema(title = "會員通道名稱")
    private String channelName;

    @Schema(title = "通道狀態")
    private Integer status;

    @Schema(title = "商戶狀態")
    private Integer merchantStatus;

    @Schema(title = "指派等級")
    private Integer memberVip;

    @Schema(title = "指派標籤")
    private Integer memberTag;
}

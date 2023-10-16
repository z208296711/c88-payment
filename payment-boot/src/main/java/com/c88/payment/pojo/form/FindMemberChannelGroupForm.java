package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "查詢會員通道群")
public class FindMemberChannelGroupForm extends BasePageQuery {

    @NotNull(message = "等級ID不得為空")
    @Schema(title = "等級ID")
    private Long vipId;

    @NotNull(message = "等級ID不得為空")
    @Schema(title = "支付ID")
    private Integer rechargeTypeId;

}

package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "找出款代付表單")
public class FindMerchantPayForm extends BasePageQuery {

    @Parameter(description = "狀態 0停用 1啟用")
    @Schema(title = "狀態", description = "0停用 1啟用")
    private Integer enable;

    @Parameter(description = "會員等級ID")
    @Schema(title = "會員等級ID")
    private Integer vipId;

    @Parameter(description = "會員標籤ID")
    @Schema(title = "會員標籤ID")
    private Integer tagId;

}

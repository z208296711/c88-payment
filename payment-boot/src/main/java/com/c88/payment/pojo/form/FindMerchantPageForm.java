package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class FindMerchantPageForm extends BasePageQuery {

    @Parameter(description = "商戶啟用狀態")
    private Integer enable;

}

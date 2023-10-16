package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "商戶列表VO")
public class MerchantVO {

    @Schema(title = "id")
    private Integer id;

    @Schema(title = "商戶名称")
    private String name;

    @Schema(title = "備註")
    private String note;

    @Schema(title = "支援指定銀行")
    private Integer isBank;

    @Schema(title = "備註")
    private String enable;

}

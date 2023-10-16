package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "前台-會員通道VO")
public class H5BankVO {

    @Schema(title = "id")
    private Long id;

    @Schema(title = "rechargeName")
    private String rechargeName;

    @Schema(title = "logo")
    private String logo;

}

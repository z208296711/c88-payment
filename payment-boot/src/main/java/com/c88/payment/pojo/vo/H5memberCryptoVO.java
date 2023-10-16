package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "前台虛擬幣")
public class H5memberCryptoVO {

    @Schema(title = "名稱")
    private String name;

    @Schema(title = "地址")
    private String address;

}

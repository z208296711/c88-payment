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
@Schema(title = "後台代理銀行卡")
public class AdminAffiliateBankCardVO {

    @Schema(title = "銀行/協議", description = "1銀行 2協議")
    private Integer type;

    /**
     * 銀行ID
     */
    @Schema(title = "銀行名稱/協議名稱")
    private String typeName;

    /**
     * 姓名
     */
    @Schema(title = "real_name")
    private String realName;

    /**
     * 卡號
     */
    @Schema(title = "卡號/提幣地址")
    private String cardNo;

}

package com.c88.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "增加餘額")
public class AddAffiliateBalanceDTO implements Serializable {

    @NotNull(message = "代理ID不得為空")
    @Schema(title = "代理ID")
    private Long affiliateId;

    @Schema(title = "代理名稱")
    private String userName;

    @NotBlank(message = "訂單號不得為空")
    @Schema(title = "訂單號")
    private String serialNo;

    @NotNull(message = "變動金額")
    @Schema(title = "變動金額(平台分數)")
    private BigDecimal amount;

    @NotNull(message = "變動金額")
    @Schema(title = "變動類型")
    private Integer type;

    @Schema(title = "備註")
    private String note = "";

    @Schema(title = "創建時間")
    private LocalDateTime gmtCreate;

    @Schema(title = "操作者")
    private String updateUser;

}

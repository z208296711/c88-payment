package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "新增會員虛擬貨幣")
public class MemberCryptoAddForm {

    @NotNull(message = "別名不得為空")
    @Schema(title = "別名")
    private String nickname;

    @NotNull(message = "協議不得為空")
    @Schema(title = "協議方式")
    private String protocol;

    @NotNull(message = "提幣地址不得為空")
    @Schema(title = "提幣地址")
    private String address;

}

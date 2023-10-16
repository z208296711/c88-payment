package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "修改會員虛擬貨幣")
public class MemberCryptoModifyForm {

    @NotNull(message = "會員虛擬貨幣ID不得為空")
    @Schema(title = "會員虛擬貨幣ID")
    private String id;

    @Schema(title = "別名")
    private String nickname;

    @Schema(title = "協議")
    private String protocol;

    @Schema(title = "提幣地址")
    private String address;

    @Schema(title = "提幣地址")
    private Integer enable;

}

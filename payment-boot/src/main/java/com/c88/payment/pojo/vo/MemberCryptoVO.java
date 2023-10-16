package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "查詢會員虛擬貨幣表單")
public class MemberCryptoVO {

    @Schema(title = "ID")
    private Integer id;

    @Schema(title = "會員ID")
    private Integer memberId;

    @Schema(title = "別名")
    private String nickname;

    @Schema(title = "協議")
    private String protocol;

    @Schema(title = "提幣地址")
    private String address;

    @Schema(title = "啟用狀態", description = "0停用1啟用")
    private Integer enable;
}

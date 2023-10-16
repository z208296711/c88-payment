package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "AffiliateBankCardVO")
public class AffiliateBankCardVO {

    @Schema(title = "id")
    private Long id;

    @Schema(title = "會員銀行卡ID")
    private Long bankId;

    @Schema(title = "會員銀行卡號")
    private String bankCardNo;

    @Schema(title = "銀行卡銀行狀態", description = "0:維護中 1:啟用中 2:排程中")
    private Integer state;

    @Schema(title = "維護時間起")
    private String startTime;

    @Schema(title = "維護時間訖")
    private String endTime;


}

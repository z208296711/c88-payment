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
@Schema(title = "前台銀行卡")
public class H5MemberBankVO {

    @Schema(title = "會員銀行卡ID")
    private Integer memberBankId;

    @Schema(title = "會員銀行卡號")
    private String memberBankCardNo;

    @Schema(title = "銀行卡銀行狀態", description = "0:維護中 1:啟用中 2:排程中")
    private Integer state;

    @Schema(title = "維護時間起")
    private String startTime;

    @Schema(title = "維護時間訖")
    private String endTime;

}

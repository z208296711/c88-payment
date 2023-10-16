package com.c88.payment.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(title = "帳務提款記錄")
public class H5MemberAccountWithdrawRecordVO {

    @Schema(title = "狀態", description = "0全部 1審核中 2審核拒絕 3出款中 4成功 5失敗")
    private Integer status;

    @Schema(title = "提款單號")
    private String withdrawNo;

    @Schema(title = "提款金額")
    private BigDecimal amount;

    @Schema(title = "提款當下餘額")
    private BigDecimal currentBalance;

    @Schema(title = "申請時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;

}

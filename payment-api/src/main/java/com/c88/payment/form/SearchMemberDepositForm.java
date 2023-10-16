package com.c88.payment.form;

import com.c88.common.core.base.BasePageQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;


@Data
@Schema(title = "查詢用戶充值")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchMemberDepositForm extends BasePageQuery {
    @Schema(title = "玩家帳號")
    String username ;

    @Schema(title = "查詢開始時間")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime beginTime;

    @Schema(title = "查詢結束時間")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime endTime;
}

package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Schema(title = "查找-代理轉帳紀錄")
public class AdminSearchAffiliateTransferForm extends BasePageQuery {

   @Parameter(description = "上級代理帳號")
   @Schema(title = "上級代理帳號")
   private String affiliateUsername;

   @Parameter(description = "代理帳號")
   @Schema(title = "代理帳號")
   private String targetAffiliateUsername;

   @Parameter(description = "時間查詢-開始")
   @Schema(title = "時間查詢-開始", description = "pattern: yyyy-MM-dd HH:mm:ss")
   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private LocalDateTime startTime;

   @Parameter(description = "時間查詢-結束")
   @Schema(title = "時間查詢-結束", description = "pattern: yyyy-MM-dd HH:mm:ss")
   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private LocalDateTime endTime;

}

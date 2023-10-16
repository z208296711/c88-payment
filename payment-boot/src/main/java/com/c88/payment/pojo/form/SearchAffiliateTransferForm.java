package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;


@Data
@Schema(title = "查找-代理轉帳紀錄")
public class SearchAffiliateTransferForm extends BasePageQuery {

   @Schema(title = "代理帳號(for /h5/affiliate/transfer)  玩家帳號（for /h5/affiliate/member/transfer）")
   private String username;

   @Schema(title = "開始時間")
   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
   private LocalDate startTime;

   @Schema(title = "結束時間")
   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
   private LocalDate endTime;

}

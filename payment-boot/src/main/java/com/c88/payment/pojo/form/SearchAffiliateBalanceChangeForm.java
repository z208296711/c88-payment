package com.c88.payment.pojo.form;

import com.c88.common.core.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;


@Data
@Schema(title = "查找-代理轉帳紀錄")
public class SearchAffiliateBalanceChangeForm extends BasePageQuery {

   @Schema(title = "帳變類型 0:提款 1:代理轉帳 2:玩家轉帳 3:微調 4:返佣")
   private List<Integer> typeList;

   @Schema(title = "開始時間")
   @DateTimeFormat(pattern = "yyyy-MM-dd")
   private LocalDate startTime;

   @Schema(title = "結束時間")
   @DateTimeFormat(pattern = "yyyy-MM-dd")
   private LocalDate endTime;

   @Schema(title = "提款密碼")
   private String withdrawPassword;

   @Schema(title = "時區，+X", example = "0(UTC), 8(Taipei)")
   private Integer gmtTime = 7;

}

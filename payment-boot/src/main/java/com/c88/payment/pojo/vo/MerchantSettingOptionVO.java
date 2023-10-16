package com.c88.payment.pojo.vo;

import com.c88.member.vo.OptionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "支付商戶選項")
public class MerchantSettingOptionVO {

    @Schema(title = "商戶Id")
    private Long id;

    @Schema(title = "支付商戶名稱")
    private String code;

    @Schema(title = "支付方式選單")
    List<OptionVO<Integer>> rechargeTypes;

}

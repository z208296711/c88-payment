package com.c88.payment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "支付方式")
public class H5RechargeVO {

    @Schema(title = "充值類型清單")
    private List<RechargeTypeVO> rechargeTypes;

    @Schema(title = "VIP對應提示訊息")
    private List<String> noteList;
}

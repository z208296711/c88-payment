package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "修改平台遊戲排序表單")
public class ModifyBankSortForm {

    @Schema(title = "修改多個排序")
    private List<BankSortForm> bankSortReq;

    @Schema(title = "進行排序拖拉的銀行縮寫")
    private String code;

    @Schema(title = "進行排序拖拉後的銀行排序")
    private Integer sort;

}

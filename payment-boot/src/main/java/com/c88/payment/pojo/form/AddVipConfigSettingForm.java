package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "新增等級設置標籤表單")
public class AddVipConfigSettingForm {

    private Integer vipConfigId;

    private List<VipConfigSettingLabelForm> labels;

    private List<VipConfigSettingNoteForm> notes;

    private List<VipConfigSettingSortForm> sorts;

    @JsonIgnore
    private String vipName;

    @JsonIgnore
    private String rechargeTypeName;
}

package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "修改會員通道群組指派方式")
public class ModifyMemberChannelGroupAssignForm {

    @Schema(title = "等級Id")
    private Integer vipId;

    @Schema(title = "支付id")
    private Integer rechargeTypeId;

    @Schema(title = "指派方式", description = "0_輪替, 1_隨機")
    private Integer type;

    @JsonIgnore
    private String vipName;

    @JsonIgnore
    private String rechargeTypeName;
}

package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

@Data
@Schema(title = "修改會員通道群組排序置頂至底表單")
public class ModifyMemberChannelGroupSortTopBottomForm {

    @Range(min = 0, max = 1, message = "修改排序方式參數錯誤")
    @Schema(title = "修改排序方式", description = "0置頂1置底")
    private Integer sortType;

    @NotNull(message = "通道群組ID不得為空")
    @Schema(title = "通道群組ID")
    private Integer id;

    @JsonIgnore
    private String vipName;

    @JsonIgnore
    private String rechargeTypeName;

    @JsonIgnore
    private String channelName;
}

package com.c88.payment.pojo.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "修改會員通道群組排序互換表單")
public class ModifyMemberChannelGroupSortForm {

    @Schema(title = "修改多個排序")
    private List<MemberChannelGroupSortForm> memberChannelGroupSortForm;

    @JsonIgnore
    private String channelName;

    @JsonIgnore
    private int seq;
}

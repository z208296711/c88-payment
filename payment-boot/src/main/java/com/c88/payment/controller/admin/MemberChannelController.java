package com.c88.payment.controller.admin;

import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.LogOpResponse;
import com.c88.common.web.log.OperationEnum;
import com.c88.member.vo.OptionVO;
import com.c88.payment.pojo.entity.MemberChannel;
import com.c88.payment.pojo.form.FindMemberChannelForm;
import com.c88.payment.pojo.form.MemberChannelForm;
import com.c88.payment.pojo.vo.MemberChannelVO;
import com.c88.payment.service.MemberChannelCommonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "『後台』會員通道管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/memberChannel")
public class MemberChannelController {

    private final MemberChannelCommonService memberChannelCommonService;

    @Operation(summary = "新增會員通道")
    @PostMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.INSERT,
            content = "new String[]{#form.id, #form.channelName}",
            menu = "menu.finance",
            menuPage = "menu.member-channel",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_deposit_channel.operation_log01")
    public Result<Boolean> insertMemberChannel(@RequestBody MemberChannelForm form) {
        return Result.judge(memberChannelCommonService.insertMemberChannel(form));
    }

    @Operation(summary = "查詢會員通道")
    @GetMapping
    public PageResult<MemberChannelVO> queryMemberChannel(@ParameterObject FindMemberChannelForm form) {
        return PageResult.success(memberChannelCommonService.findMemberChannels(form));
    }

    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "#form.channelName!=null?new String[]{#form.id, #form.channelName}:" +
                    "new String[]{#form.channelName,#form.status==0?'停用':'啟用'}",
            desc = "\"編輯會員通道：ID{0} {1}\n" +
                    "修改前{2}\n" +
                    "修改後{3}\"" +
                    " OR " +
                    "通道狀態開關：{會員通道名稱}設置為{啟用/停用}",
            menu = "menu.finance",
            menuPage = "menu.member-channel",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "#form.channelName!=null?'be_deposit_channel.operation_log02':'be_deposit_channel_group.operation_log05'")
    @Operation(summary = "更新會員通道")
    @PutMapping
    public Result<LogOpResponse> updateMemberChannel(@RequestBody MemberChannelForm form) {
        MemberChannel memberChannel = memberChannelCommonService.getById(form.getId());
        boolean isOnlyUpdateStatus = form.getChannelName() == null;
        if (isOnlyUpdateStatus) {
            form.setChannelName(memberChannel.getChannelName());
        }
        memberChannelCommonService.updateMemberChannel(form, isOnlyUpdateStatus);
        LogOpResponse<MemberChannel, MemberChannelForm> response = new LogOpResponse<>();
        if (!isOnlyUpdateStatus) {
            response.setBefore(memberChannel);
            response.setAfter(form);
        }
        return Result.success(response);
    }

    @Operation(summary = "刪除會員通道")
    @DeleteMapping("/{id}")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DELETE,
            content = "new String[]{#id, #form.channelName}",
            menu = "menu.finance",
            menuPage = "menu.member-channel",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_deposit_channel.operation_log03")
    public Result<Boolean> deleteMemberChannel(@PathVariable Long id, MemberChannelForm form) {
        MemberChannel memberChannel = memberChannelCommonService.getById(id);
        form.setChannelName(memberChannel != null ? memberChannel.getChannelName() : null);
        return Result.judge(memberChannelCommonService.delMemberChannel(id));
    }

    @Operation(summary = "會員通道項目")
    @GetMapping("/option")
    public Result<List<OptionVO<Long>>> findMemberChannelOption() {
        return Result.success(memberChannelCommonService.findMemberChannelOption());
    }

    @Operation(summary = "會員通道使用的所有VIP ID")
    @GetMapping("/use/vip")
    public Result<Set<Long>> findMemberChannelUseVipIds() {
        return Result.success(memberChannelCommonService.findMemberChannelUseVipIds());
    }

}

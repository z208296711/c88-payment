package com.c88.payment.controller.admin;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.LogOpResponse;
import com.c88.common.web.log.OperationEnum;
import com.c88.member.api.MemberFeignClient;
import com.c88.payment.pojo.entity.MemberChannelGroup;
import com.c88.payment.pojo.entity.MemberChannelVipConfigSetting;
import com.c88.payment.pojo.form.*;
import com.c88.payment.pojo.vo.MemberChannelGroupVO;
import com.c88.payment.pojo.vo.VipConfigSettingLabelVO;
import com.c88.payment.pojo.vo.VipConfigSettingNoteVO;
import com.c88.payment.pojo.vo.VipConfigSettingSortVO;
import com.c88.payment.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Tag(name = "『後台』會員通道群組管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member/channel/group")
public class MemberChannelGroupController {

    private final IMemberChannelVipConfigSettingService iMemberChannelVipConfigSettingService;

    private final MemberChannelCommonService memberChannelCommon;

    private final MemberFeignClient memberFeignClient;

    private final IRechargeTypeService iRechargeTypeService;

    private final IMemberChannelGroupService iMemberChannelGroupService;

    private final IMemberChannelService iMemberChannelService;

    @Operation(summary = "查找會員通道群組支付標籤")
    @GetMapping("/label/{vipId}")
    public Result<List<VipConfigSettingLabelVO>> getMemberChannelGroupLabel(@PathVariable("vipId") Integer vipId) {
        return Result.success(iMemberChannelVipConfigSettingService.getVipConfigSettingLabel(vipId));
    }

    @Operation(summary = "新增會員通道群組支付標籤")
    @PutMapping("/label")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.vipName}",
            menu = "menu.finance",
            menuPage = "menu.member-channel",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_deposit_channel_group.operation_log01")
    public Result<LogOpResponse> insertMemberChannelGroupLabel(@RequestBody AddVipConfigSettingForm form) {
        Map<Integer, String> map = memberFeignClient.findMemberVipConfigMap().getData();
        form.setVipName(map.get(form.getVipConfigId()));
        LogOpResponse response = new LogOpResponse<>();
        MemberChannelVipConfigSetting memberChannelVipConfigSetting = iMemberChannelVipConfigSettingService.getById(form.getVipConfigId());
        if (memberChannelVipConfigSetting != null && memberChannelVipConfigSetting.getLabel() != null) {
            response.setBefore(memberChannelVipConfigSetting.getLabel());
        } else {
            response.setBefore(newLabel(form));
        }
        response.setAfter(form.getLabels());
        iMemberChannelVipConfigSettingService.addVipConfigSettingLabel(form);
        return Result.success(response);
    }

    @Operation(summary = "查找會員通道群組支付提示訊息")
    @GetMapping("/note/{vipId}")
    public Result<List<VipConfigSettingNoteVO>> getMemberChannelGroupNote(@PathVariable("vipId") Integer vipId) {
        return Result.success(iMemberChannelVipConfigSettingService.getVipConfigSettingNote(vipId));
    }

    @Operation(summary = "新增會員通道群組支付提示訊息")
    @PutMapping("/note")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.vipName}",
            menu = "menu.finance",
            menuPage = "menu.member-channel",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_deposit_channel_group.operation_log02")
    public Result<LogOpResponse> insertMemberChannelGroupNote(@RequestBody AddVipConfigSettingForm form) {
        Map<Integer, String> map = memberFeignClient.findMemberVipConfigMap().getData();
        form.setVipName(map.get(form.getVipConfigId()));
        LogOpResponse response = new LogOpResponse<>();
        MemberChannelVipConfigSetting memberChannelVipConfigSetting = iMemberChannelVipConfigSettingService.getById(form.getVipConfigId());
        if (memberChannelVipConfigSetting != null && memberChannelVipConfigSetting.getNote() != null) {
            response.setBefore(memberChannelVipConfigSetting.getNote());
        } else {
            response.setBefore(newNote(form));
        }
        response.setAfter(form.getNotes());
        iMemberChannelVipConfigSettingService.addVipConfigSettingNote(form);
        return Result.success(response);
    }

    private JSONArray newNote(AddVipConfigSettingForm form) {
        JSONArray note = new JSONArray();
        if (form.getNotes() != null) {
            note.addAll(form.getNotes().parallelStream().map(e -> {
                VipConfigSettingNoteForm n = new VipConfigSettingNoteForm();
                n.setNote("");
                return n;
            }).collect(Collectors.toList()));
        }
        return note;
    }

    private JSONArray newLabel(AddVipConfigSettingForm form) {
        JSONArray note = new JSONArray();
        if (form.getLabels() != null) {
            note.addAll(form.getLabels().parallelStream().map(e -> {
                VipConfigSettingLabelForm l = new VipConfigSettingLabelForm();
                BeanUtils.copyProperties(e, l);
                l.setLabel("");
                return l;
            }).collect(Collectors.toList()));
        }
        return note;
    }

    @Operation(summary = "查找會員通道群組支付排序")
    @GetMapping("/rechargeType/sort/{vipId}")
    public Result<List<VipConfigSettingSortVO>> getMemberChannelGroupSort(@PathVariable("vipId") Integer vipId) {
        return Result.success(iMemberChannelVipConfigSettingService.getVipConfigSettingSort(vipId));
    }

    @Operation(summary = "新增會員通道群組支付排序")
    @PutMapping("/rechargeType/sort")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.vipName}",
            menu = "menu.finance",
            menuPage = "menu.member-channel",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_deposit_channel_group.operation_log03")
    public Result<Boolean> insertMemberChannelGroupSort(@RequestBody AddVipConfigSettingForm form) {
        Map<Integer, String> map = memberFeignClient.findMemberVipConfigMap().getData();
        form.setVipName(map.get(form.getVipConfigId()));
        return Result.judge(iMemberChannelVipConfigSettingService.addVipConfigSettingSort(form));
    }

    @Operation(summary = "查找會員通道群組")
    @GetMapping
    public PageResult<MemberChannelGroupVO> findMemberChannelGroup(@ParameterObject FindMemberChannelGroupForm form) {
        return PageResult.success(memberChannelCommon.findMemberChannelGroup(form));
    }

    @Operation(summary = "修改指派方式")
    @PutMapping("/assignment")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.vipName, #form.rechargeTypeName,#form.type==0?'輪替':'隨機'}",
            menu = "menu.finance",
            menuPage = "menu.member-channel",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_deposit_channel_group.operation_log04")
    public Result<Boolean> modifyAssignment(@RequestBody ModifyMemberChannelGroupAssignForm form) {
        Map<Integer, String> map = memberFeignClient.findMemberVipConfigMap().getData();
        form.setVipName(map.get(form.getVipId()));
        form.setRechargeTypeName("{{" + iRechargeTypeService.getById(form.getRechargeTypeId()).getName() + "}}");// recharge type name 為 i18n，需加{{}}給前端取代
        return Result.judge(memberChannelCommon.modifyAssign(form));
    }

    @Operation(summary = "修改通道群組排序 互換", description = "互換")
    @PutMapping("/sort")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.channelName,#form.seq}",
            menu = "menu.finance",
            menuPage = "menu.member-channel",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_deposit_channel.operation_log04")
    public Result<Boolean> modifyMemberChannelGroupSort(@Validated @RequestBody ModifyMemberChannelGroupSortForm form) {
        List<MemberChannelGroup> memberChannelGroups = iMemberChannelGroupService.list(new LambdaQueryWrapper<MemberChannelGroup>()
                .in(MemberChannelGroup::getId, form.getMemberChannelGroupSortForm().stream().map(MemberChannelGroupSortForm::getId)
                        .collect(Collectors.toList()))
                .orderByAsc(MemberChannelGroup::getSort));
        MemberChannelGroup memberChannelGroup = compareSort(memberChannelGroups, form.getMemberChannelGroupSortForm());
        if (memberChannelGroup != null) {
            form.setChannelName(iMemberChannelService.getById(memberChannelGroup.getMemberChanelId()).getChannelName());
            form.setSeq(memberChannelGroup.getSort());
        }
        return Result.success(memberChannelCommon.modifyMemberChannelGroupSort(form));
    }

    private MemberChannelGroup compareSort(List<MemberChannelGroup> list, List<MemberChannelGroupSortForm> formList) {
        for (int i = 0; i < list.size(); i++) {
            if (!Objects.equals(list.get(i).getId(), formList.get(i).getId())) {
                MemberChannelGroup memberChannelGroup = list.get(i);
                memberChannelGroup.setSort(i + 1);
                return memberChannelGroup;
            }
        }
        return null;
    }

    @Operation(summary = "修改通道群組排序 置頂置底", description = "0置頂1置底")
    @PutMapping("/sort/top/bottom")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.vipName, #form.rechargeTypeName,#form.channelName,#form.sortType==0?'置頂':'置底'}",
            menu = "menu.finance",
            menuPage = "menu.member-channel",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_deposit_channel_group.operation_log06")
    public Result<Boolean> modifyPlatformGameSortTopBottom(@Validated @RequestBody ModifyMemberChannelGroupSortTopBottomForm form) {
        MemberChannelGroup memberChannelGroup = iMemberChannelGroupService.getById(form.getId());
        Map<Integer, String> map = memberFeignClient.findMemberVipConfigMap().getData();
        form.setVipName(map.get(memberChannelGroup.getVipConfigId().intValue()));
        form.setRechargeTypeName("{{" + iRechargeTypeService.getById(memberChannelGroup.getRechargeTypeId()).getName() + "}}");// recharge type name 為 i18n，需加{{}}給前端取代
        form.setChannelName(iMemberChannelService.getById(memberChannelGroup.getMemberChanelId()).getChannelName());
        return Result.success(memberChannelCommon.modifyMemberChannelGroupTopBottom(form));
    }
}

package com.c88.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.member.vo.OptionVO;
import com.c88.payment.mapper.MemberChannelVipConfigSettingMapper;
import com.c88.payment.mapstruct.MemberChannelVipConfigSettingConverter;
import com.c88.payment.pojo.entity.MemberChannelVipConfigSetting;
import com.c88.payment.pojo.form.AddVipConfigSettingForm;
import com.c88.payment.pojo.vo.VipConfigSettingLabelVO;
import com.c88.payment.pojo.vo.VipConfigSettingNoteVO;
import com.c88.payment.pojo.vo.VipConfigSettingSortVO;
import com.c88.payment.service.IMemberChannelVipConfigSettingService;
import com.c88.payment.service.IRechargeTypeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class MemberChannelVipConfigSettingServiceImpl extends ServiceImpl<MemberChannelVipConfigSettingMapper, MemberChannelVipConfigSetting>
        implements IMemberChannelVipConfigSettingService {

    private final MemberChannelVipConfigSettingConverter memberChannelVipConfigSettingConverter;

    private final IRechargeTypeService iRechargeTypeService;

    @Override
    public Boolean addVipConfigSettingLabel(AddVipConfigSettingForm form) {
        MemberChannelVipConfigSetting memberChannelVipConfigSetting = memberChannelVipConfigSettingConverter.toEntity(form);
        return this.saveOrUpdate(memberChannelVipConfigSetting);
    }

    @Override
    public List<VipConfigSettingLabelVO> getVipConfigSettingLabel(Integer vipId) {
        MemberChannelVipConfigSetting setting = this.lambdaQuery()
                .select(MemberChannelVipConfigSetting::getLabel)
                .eq(MemberChannelVipConfigSetting::getVipConfigId, vipId)
                .one();
        JSONArray jsonArray;
        List<OptionVO<Integer>> optionVOS = iRechargeTypeService.findRechargeTypeOptionByVip(vipId);
        List<VipConfigSettingLabelVO> settingLabelVOS;
        // 對空值做處理
        if (Objects.isNull(setting)) {
            settingLabelVOS = optionVOS.stream().map(x -> {
                VipConfigSettingLabelVO vipConfigSettingLabelVO = new VipConfigSettingLabelVO();
                vipConfigSettingLabelVO.setLabel("");
                vipConfigSettingLabelVO.setRechargeTypeId(x.getValue());
                vipConfigSettingLabelVO.setRechargeTypeName(x.getLabel());
                return vipConfigSettingLabelVO;
            }).collect(Collectors.toList());
        } else {
            jsonArray = setting.getLabel();
            settingLabelVOS = JSONArray.parseArray(JSON.toJSONString(jsonArray), VipConfigSettingLabelVO.class);
            Map<Integer, String> settingLabelVOMap = settingLabelVOS
                    .stream()
                    .collect(Collectors.toMap(VipConfigSettingLabelVO::getRechargeTypeId, VipConfigSettingLabelVO::getLabel));

            settingLabelVOS = optionVOS.stream().map(x -> {
                VipConfigSettingLabelVO vipConfigSettingLabelVO = new VipConfigSettingLabelVO();
                vipConfigSettingLabelVO.setLabel(settingLabelVOMap.getOrDefault(x.getValue(), ""));
                vipConfigSettingLabelVO.setRechargeTypeId(x.getValue());
                vipConfigSettingLabelVO.setRechargeTypeName(x.getLabel());
                return vipConfigSettingLabelVO;
            }).collect(Collectors.toList());
        }

        return settingLabelVOS;
    }

    @Override
    public Boolean addVipConfigSettingNote(AddVipConfigSettingForm form) {
        MemberChannelVipConfigSetting memberChannelVipConfigSetting = memberChannelVipConfigSettingConverter.toEntity(form);
        return this.saveOrUpdate(memberChannelVipConfigSetting);
    }

    @Override
    public List<VipConfigSettingNoteVO> getVipConfigSettingNote(Integer vipId) {
        MemberChannelVipConfigSetting setting = this.lambdaQuery()
                .select(MemberChannelVipConfigSetting::getNote)
                .eq(MemberChannelVipConfigSetting::getVipConfigId, vipId)
                .one();
        JSONArray jsonArray;
        // 對空值做處理
        if (Objects.isNull(setting)) {
            setting = new MemberChannelVipConfigSetting();
        }

        jsonArray = setting.getNote();
        List<VipConfigSettingNoteVO> settingNoteVOS = JSONArray.parseArray(JSON.toJSONString(jsonArray), VipConfigSettingNoteVO.class);
        //補滿成5個
        for (int i = settingNoteVOS.size(); i < 5; i++) {
            settingNoteVOS.add(new VipConfigSettingNoteVO(""));
        }

        return settingNoteVOS;
    }

    @Override
    public Boolean addVipConfigSettingSort(AddVipConfigSettingForm form) {
        MemberChannelVipConfigSetting memberChannelVipConfigSetting = memberChannelVipConfigSettingConverter.toEntity(form);
        return this.saveOrUpdate(memberChannelVipConfigSetting);
    }

    @Override
    public List<VipConfigSettingSortVO> getVipConfigSettingSort(Integer vipId) {
        // 取得指定VIP設定排序
        MemberChannelVipConfigSetting setting = this.lambdaQuery()
                .select(MemberChannelVipConfigSetting::getSort)
                .eq(MemberChannelVipConfigSetting::getVipConfigId, vipId)
                .oneOpt()
                .orElse(MemberChannelVipConfigSetting.builder().build());

        List<VipConfigSettingSortVO> vipConfigSettingSortVOS = Optional.ofNullable(JSON.parseArray(JSON.toJSONString(setting.getSort()), VipConfigSettingSortVO.class))
                .orElse(List.of(VipConfigSettingSortVO.builder().build()));

        // 取得目前有的充值類型
        return iRechargeTypeService.lambdaQuery()
                .list()
                .stream()
                .map(rechargeType ->
                        vipConfigSettingSortVOS.stream()
                                .filter(filter -> Objects.equals(filter.getRechargeTypeId(), rechargeType.getId()))
                                .findFirst()
                                .orElse(
                                        VipConfigSettingSortVO.builder()
                                                .rechargeTypeId(rechargeType.getId())
                                                .rechargeTypeName(rechargeType.getName())
                                                .sort(rechargeType.getSort())
                                                .build()
                                )
                )
                .sorted(Comparator.comparing(VipConfigSettingSortVO::getSort))
                .collect(Collectors.toList());

        // JSONArray jsonArray;
        // // 對空值做處理
        // if (Objects.isNull(setting)) {
        //     List<RechargeType> rechargeTypeVOS = iRechargeTypeService.lambdaQuery().orderByAsc(RechargeType::getSort).list();
        //     List<VipConfigSettingSortVO> settingSortVOS = rechargeTypeVOS.stream().map(x -> {
        //         VipConfigSettingSortVO vipConfigSettingSortVO = new VipConfigSettingSortVO();
        //         vipConfigSettingSortVO.setRechargeTypeId(x.getId());
        //         vipConfigSettingSortVO.setRechargeTypeName(x.getName());
        //         vipConfigSettingSortVO.setSort(x.getSort());
        //         return vipConfigSettingSortVO;
        //     }).collect(Collectors.toList());
        //     jsonArray = new JSONArray(JSON.parseArray(JSON.toJSONString(settingSortVOS)));
        // } else {
        //     jsonArray = setting.getSort();
        // }

        // return JSONArray.parseArray(JSON.toJSONString(jsonArray), VipConfigSettingSortVO.class);
    }
}

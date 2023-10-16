package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.entity.MemberChannelVipConfigSetting;
import com.c88.payment.pojo.form.AddVipConfigSettingForm;
import com.c88.payment.pojo.vo.VipConfigSettingLabelVO;
import com.c88.payment.pojo.vo.VipConfigSettingNoteVO;
import com.c88.payment.pojo.vo.VipConfigSettingSortVO;

import java.util.List;

public interface IMemberChannelVipConfigSettingService extends IService<MemberChannelVipConfigSetting> {

    Boolean addVipConfigSettingLabel(AddVipConfigSettingForm form);

    List<VipConfigSettingLabelVO> getVipConfigSettingLabel(Integer vipId);

    Boolean addVipConfigSettingNote(AddVipConfigSettingForm form);

    List<VipConfigSettingNoteVO> getVipConfigSettingNote(Integer vipId);

    Boolean addVipConfigSettingSort(AddVipConfigSettingForm form);

    List<VipConfigSettingSortVO> getVipConfigSettingSort(Integer vipId);
}

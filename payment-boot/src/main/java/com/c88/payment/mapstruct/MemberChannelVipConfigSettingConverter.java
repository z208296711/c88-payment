package com.c88.payment.mapstruct;

import com.alibaba.fastjson.JSON;
import com.c88.payment.pojo.entity.MemberChannelVipConfigSetting;
import com.c88.payment.pojo.form.AddVipConfigSettingForm;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberChannelVipConfigSettingConverter {

    MemberChannelVipConfigSetting toEntity(AddVipConfigSettingForm form);

    @AfterMapping
    default void customMapping(@MappingTarget MemberChannelVipConfigSetting entity, AddVipConfigSettingForm source) {
        entity.setLabel(JSON.parseArray(JSON.toJSONString(source.getLabels())));
        entity.setNote(JSON.parseArray(JSON.toJSONString(source.getNotes())));
        entity.setSort(JSON.parseArray(JSON.toJSONString(source.getSorts())));
    }
}

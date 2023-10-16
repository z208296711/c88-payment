package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.pojo.entity.MemberChannel;
import com.c88.payment.pojo.vo.MemberChannelGroupVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberChannelGroupConverter extends BaseConverter<MemberChannel, MemberChannelGroupVO> {
}

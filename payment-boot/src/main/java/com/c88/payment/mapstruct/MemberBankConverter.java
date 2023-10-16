package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.pojo.entity.MemberBank;
import com.c88.payment.pojo.vo.MemberBankVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberBankConverter extends BaseConverter<MemberBank, MemberBankVO> {
}

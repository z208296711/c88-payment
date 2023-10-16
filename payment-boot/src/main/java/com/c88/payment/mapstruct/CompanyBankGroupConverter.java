package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.pojo.entity.CompanyBankCardGroup;
import com.c88.payment.pojo.form.CompanyBankCardGroupForm;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyBankGroupConverter extends BaseConverter<CompanyBankCardGroup, CompanyBankCardGroupForm> {
}

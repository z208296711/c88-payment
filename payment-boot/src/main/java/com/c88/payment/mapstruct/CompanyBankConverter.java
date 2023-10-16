package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.pojo.entity.CompanyBankCard;
import com.c88.payment.pojo.form.CompanyBankCardForm;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyBankConverter extends BaseConverter<CompanyBankCard, CompanyBankCardForm> {
}

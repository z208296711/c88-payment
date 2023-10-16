package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.pojo.entity.CompanyBankCard;
import com.c88.payment.pojo.vo.CompanyBankCardVO;
import com.c88.payment.pojo.vo.H5CompanyBankCardVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyBankCardConverter extends BaseConverter<CompanyBankCard, CompanyBankCardVO> {

    H5CompanyBankCardVO toH5CompanyBankCardVO(CompanyBankCard companyBankCard);

}

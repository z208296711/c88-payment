package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.pojo.entity.MerchantPay;
import com.c88.payment.pojo.form.ModifyMerchantPayForm;
import com.c88.payment.pojo.vo.MerchantPayVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MerchantPayConverter extends BaseConverter<MerchantPay, MerchantPayVO> {

    MerchantPay toEntity(ModifyMerchantPayForm form);

}

package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.pojo.entity.MerchantRechargeType;
import com.c88.payment.pojo.vo.MerchantRechargeTypeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MerchantRechargeTypeConverter extends BaseConverter<MerchantRechargeType, MerchantRechargeTypeVO> {

    @Mappings({
            @Mapping(target = "name", source = "rechargeTypeName")
    })
    MerchantRechargeTypeVO toVo(MerchantRechargeType entity);

}

package com.c88.payment.mapstruct;

import com.c88.payment.pojo.entity.AffiliateTransferRecord;
import com.c88.payment.pojo.vo.AdminAffiliateTransferRecordVO;
import com.c88.payment.pojo.vo.AffiliateTransferRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AffiliateTransferRecordConverter {

    @Mappings({
            @Mapping(target = "affiliateId", source = "targetAffiliateId"),
            @Mapping(target = "affiliateUsername", source = "targetAffiliateUsername")
    })
    AffiliateTransferRecordVO toVo(AffiliateTransferRecord entity);

    AdminAffiliateTransferRecordVO toAdminAffiliateTransferRecordVO(AffiliateTransferRecord entity);
}

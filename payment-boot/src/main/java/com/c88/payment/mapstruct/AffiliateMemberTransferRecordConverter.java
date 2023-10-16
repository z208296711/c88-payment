package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.pojo.entity.AffiliateMemberTransferRecord;
import com.c88.payment.pojo.entity.AffiliateTransferRecord;
import com.c88.payment.pojo.vo.AdminAffiliateMemberTransferRecordVO;
import com.c88.payment.pojo.vo.AffiliateMemberTransferRecordVO;
import com.c88.payment.pojo.vo.AffiliateTransferRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AffiliateMemberTransferRecordConverter extends BaseConverter<AffiliateMemberTransferRecord, AffiliateMemberTransferRecordVO> {

    AdminAffiliateMemberTransferRecordVO toAdminAffiliateMemberTransferRecordVO(AffiliateMemberTransferRecord entity);
}

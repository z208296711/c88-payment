package com.c88.payment.mapstruct;

import com.c88.payment.pojo.entity.MemberBonusRecord;
import com.c88.payment.pojo.vo.MemberBonusRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberBonusRecordConverter {

    MemberBonusRecordVO toVO(MemberBonusRecord entity);

}

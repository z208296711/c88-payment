package com.c88.payment.mapstruct;

import com.c88.payment.pojo.entity.BalanceChangeRecord;
import com.c88.payment.pojo.vo.AdminBalanceChangeRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BalanceChangeRecordConverter {

    AdminBalanceChangeRecordVO toVo(BalanceChangeRecord entity);

}

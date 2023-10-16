package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.dto.PaymentAffiliateBalanceDTO;
import com.c88.payment.pojo.entity.AffiliateBalance;
import com.c88.payment.pojo.entity.AffiliateBalanceChangeRecord;
import com.c88.payment.pojo.vo.AffiliateBalanceChangeRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AffiliateBalanceConverter extends BaseConverter<AffiliateBalance, PaymentAffiliateBalanceDTO> {


}

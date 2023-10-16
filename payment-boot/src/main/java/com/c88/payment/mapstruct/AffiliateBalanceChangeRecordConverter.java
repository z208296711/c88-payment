package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.pojo.entity.AffiliateBalanceChangeRecord;
import com.c88.payment.pojo.entity.AffiliateMemberTransferRecord;
import com.c88.payment.pojo.vo.AffiliateBalanceChangeRecordVO;
import com.c88.payment.pojo.vo.AffiliateMemberTransferRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AffiliateBalanceChangeRecordConverter extends BaseConverter<AffiliateBalanceChangeRecord, AffiliateBalanceChangeRecordVO> {

    default AffiliateBalanceChangeRecordVO toVo(AffiliateBalanceChangeRecord entity, boolean showBalance) {
        AffiliateBalanceChangeRecordVO vo = this.toVo(entity);
        if (!showBalance) {
            vo.setAfterBalance("******");
        }
        return vo;
    }

    default String afterBalance(BigDecimal afterBalance) {
        return afterBalance.stripTrailingZeros().toPlainString();
    }
}

package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.payment.dto.AddAffiliateBalanceDTO;
import com.c88.payment.dto.AffiliateBalanceChangeRecordDTO;
import com.c88.payment.enums.AffiliateBalanceChangeTypeEnum;
import com.c88.payment.pojo.entity.AffiliateBalanceChangeRecord;
import com.c88.payment.service.IAffiliateBalanceChangeRecordService;
import com.c88.payment.mapper.AffiliateBalanceChangeRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class AffiliateBalanceChangeRecordServiceImpl extends ServiceImpl<AffiliateBalanceChangeRecordMapper, AffiliateBalanceChangeRecord>
    implements IAffiliateBalanceChangeRecordService {
    @Override
    public List<AffiliateBalanceChangeRecordDTO> findChangeRecordByAffiliateIdDate(List<Long> affiliateX, LocalDateTime start, LocalDateTime end){
        return this.lambdaQuery()
                .eq(AffiliateBalanceChangeRecord::getType, AffiliateBalanceChangeTypeEnum.ADMIN_RECHARGE.getValue())
                .in(AffiliateBalanceChangeRecord::getAffiliateId,affiliateX)
                .between(AffiliateBalanceChangeRecord::getGmtCreate,start,end).list()
                .stream().map(this::convertDTO).collect(Collectors.toList());
    }

    private AffiliateBalanceChangeRecordDTO convertDTO(AffiliateBalanceChangeRecord r){
        return AffiliateBalanceChangeRecordDTO.builder().affiliateId(r.getAffiliateId()).amount(r.getAmount()).build();
    }
}





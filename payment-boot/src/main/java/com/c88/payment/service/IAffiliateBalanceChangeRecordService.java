package com.c88.payment.service;

import com.c88.payment.dto.AffiliateBalanceChangeRecordDTO;
import com.c88.payment.pojo.entity.AffiliateBalanceChangeRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 */
public interface IAffiliateBalanceChangeRecordService extends IService<AffiliateBalanceChangeRecord> {
    List<AffiliateBalanceChangeRecordDTO> findChangeRecordByAffiliateIdDate(List<Long> affiliateX, LocalDateTime start, LocalDateTime end);

}

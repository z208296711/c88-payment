package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.dto.AddAffiliateBalanceDTO;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.dto.PaymentAffiliateBalanceDTO;
import com.c88.payment.pojo.entity.AffiliateBalance;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 */
public interface IAffiliateBalanceService extends IService<AffiliateBalance> {

    List<PaymentAffiliateBalanceDTO> findPaymentAffiliateBalanceDTO(List<Long> affiliateIds);

    AffiliateBalance findByAffiliateId(Long affiliateId);

    AffiliateBalance findByUsername(String username);

    BigDecimal addBalance(AddAffiliateBalanceDTO addBalanceDTO);
    BigDecimal addAdminBalance(AddAffiliateBalanceDTO addBalanceDTO);

}

package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.affiliate.api.dto.AffiliateInfoDTO;
import com.c88.affiliate.api.feign.AffiliateFeignClient;
import com.c88.common.core.constant.TopicConstants;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.UserUtils;
import com.c88.payment.dto.AddAffiliateBalanceDTO;
import com.c88.payment.dto.PaymentAffiliateBalanceDTO;
import com.c88.payment.mapper.AffiliateBalanceMapper;
import com.c88.payment.mapstruct.AffiliateBalanceConverter;
import com.c88.payment.pojo.entity.AffiliateBalance;
import com.c88.payment.pojo.entity.AffiliateBalanceChangeRecord;
import com.c88.payment.service.IAffiliateBalanceChangeRecordService;
import com.c88.payment.service.IAffiliateBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateBalanceServiceImpl extends ServiceImpl<AffiliateBalanceMapper, AffiliateBalance> implements IAffiliateBalanceService {

    private final AffiliateFeignClient affiliateFeignClient;

    private final AffiliateBalanceConverter affiliateBalanceConverter;

    private final IAffiliateBalanceChangeRecordService iAffiliateBalanceChangeRecordService;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Override
    public List<PaymentAffiliateBalanceDTO> findPaymentAffiliateBalanceDTO(List<Long> affiliateIds) {
        return this.lambdaQuery()
                .in(AffiliateBalance::getAffiliateId, affiliateIds)
                .list()
                .stream()
                .map(affiliateBalanceConverter::toVo)
                .collect(Collectors.toList());
    }

    @Override
    public AffiliateBalance findByAffiliateId(Long affiliateId) {
        return this.lambdaQuery()
                .eq(AffiliateBalance::getAffiliateId, affiliateId)
                .oneOpt()
                .orElseGet(() -> {
                            Result<AffiliateInfoDTO> memberResult = affiliateFeignClient.getAffiliateInfoById(affiliateId);
                            if (!Result.isSuccess(memberResult)) {
                                throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
                            }
                            AffiliateInfoDTO memberInfoDTO = memberResult.getData();
                            AffiliateBalance memberBalance = AffiliateBalance.builder()
                                    .affiliateId(memberInfoDTO.getId())
                                    .username(memberInfoDTO.getUsername())
                                    .balance(BigDecimal.ZERO)
                                    .build();
                            this.save(memberBalance);
                            return memberBalance;
                        }
                );
    }

    @Override
    public AffiliateBalance findByUsername(String username) {
        return this.lambdaQuery()
                .eq(AffiliateBalance::getUsername, username)
                .oneOpt()
                .orElseGet(() -> {
                            Result<AffiliateInfoDTO> memberResult = affiliateFeignClient.getAffiliateInfoByUsername(username);
                            if (!Result.isSuccess(memberResult)) {
                                throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
                            }
                            AffiliateInfoDTO memberInfoDTO = memberResult.getData();
                            AffiliateBalance memberBalance = AffiliateBalance.builder()
                                    .affiliateId(memberInfoDTO.getId())
                                    .username(memberInfoDTO.getUsername())
                                    .balance(BigDecimal.ZERO)
                                    .build();
                            this.save(memberBalance);
                            return memberBalance;
                        }
                );
    }

    @Override
    @Transactional
    public BigDecimal addBalance(AddAffiliateBalanceDTO addBalanceDTO) {
        AffiliateBalance affiliateBalance = this.findByAffiliateId(addBalanceDTO.getAffiliateId());
        addBalanceDTO.setUserName(affiliateBalance.getUsername());
        BigDecimal beforeBalance = affiliateBalance.getBalance();
        BigDecimal afterBalance = affiliateBalance.getBalance().add(addBalanceDTO.getAmount());
        if (afterBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ResultCode.AFFILIATE_BALANCE_NOT_ENOUGH);
        }
        affiliateBalance.setBalance(afterBalance);
        boolean updateRes = this.updateById(affiliateBalance);
        if (Boolean.TRUE.equals(updateRes)) {
            iAffiliateBalanceChangeRecordService.save(AffiliateBalanceChangeRecord.builder()
                    .affiliateId(affiliateBalance.getAffiliateId())
                    .affiliateUsername(affiliateBalance.getUsername())
                    .type(addBalanceDTO.getType())
                    .serialNo(addBalanceDTO.getSerialNo())
                    .amount(addBalanceDTO.getAmount())
                    .beforeBalance(beforeBalance)
                    .afterBalance(afterBalance)
                    .note(addBalanceDTO.getNote())
                    .build());
        }
        return afterBalance;
    }

    @Override
    public BigDecimal addAdminBalance(AddAffiliateBalanceDTO addBalanceDTO) {
        addBalanceDTO.setUpdateUser(UserUtils.getUsername());
        BigDecimal afterBalance = addBalance(addBalanceDTO);
        kafkaTemplate.send(TopicConstants.AFFILIATE_OPERATION_LOG,addBalanceDTO);
        return afterBalance;
    }


}





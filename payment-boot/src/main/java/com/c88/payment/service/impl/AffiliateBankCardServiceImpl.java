package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.affiliate.api.dto.AffiliateInfoDTO;
import com.c88.affiliate.api.feign.AffiliateFeignClient;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.payment.mapper.AffiliateBankCardMapper;
import com.c88.payment.pojo.entity.AffiliateBankCard;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.form.AffiliateBankCardForm;
import com.c88.payment.pojo.vo.AffiliateBankCardVO;
import com.c88.payment.service.IAffiliateBankCardService;
import com.c88.payment.service.IBankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateBankCardServiceImpl extends ServiceImpl<AffiliateBankCardMapper, AffiliateBankCard> implements IAffiliateBankCardService {

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

    private final AffiliateFeignClient affiliateFeignClient;

    private final IBankService iBankService;

    @Override
    public List<AffiliateBankCardVO> findAffiliateBankCard(Long affiliateId) {
        List<Bank> list = iBankService.list();
        Map<Long, Bank> bankDTOMap = list.stream().collect(Collectors.toMap(Bank::getId, Function.identity()));
        return this.lambdaQuery()
                .eq(AffiliateBankCard::getAffiliateId, affiliateId)
                .eq(AffiliateBankCard::getEnable, EnableEnum.START.getCode())
                .list()
                .stream()
                .map(x -> {
                    Bank bankDTO = bankDTOMap.getOrDefault(x.getBankId(), new Bank());
                    AffiliateBankCardVO affiliateBankCardVO = new AffiliateBankCardVO();
                    affiliateBankCardVO.setId(x.getId());
                    affiliateBankCardVO.setState(bankDTO.getState());
                    affiliateBankCardVO.setStartTime(bankDTO.getDailyEnable() != 0 ? timeFormatter.format(bankDTO.getDailyStartTime()) : (bankDTO.getAssignEnable() != 0 ? dateTimeFormatter.format(bankDTO.getAssignStartTime()) : null));
                    affiliateBankCardVO.setEndTime(bankDTO.getDailyEnable() != 0 ? timeFormatter.format(bankDTO.getDailyEndTime()) : (bankDTO.getAssignEnable() != 0 ? dateTimeFormatter.format(bankDTO.getAssignEndTime()) : null));
                    affiliateBankCardVO.setBankId(x.getBankId());
                    affiliateBankCardVO.setBankCardNo(x.getCardNo());
                    return affiliateBankCardVO;
                }).collect(Collectors.toList());
    }

    @Override
    public Boolean deleteBankCard(Long id) {
        return this.removeById(id);
    }


    @Transactional
    public Boolean addAffiliateBankCard(Long affiliateId, AffiliateBankCardForm form) {
        // 檢查銀行卡不得重複
        this.checkBankCardIsExist(form.getCardNo());

        // 檢查無原真實姓名時寫入真實姓名,反之檢查真實姓名
        Result<AffiliateInfoDTO> memberResult = affiliateFeignClient.getAffiliateInfoById(affiliateId);
        if (!Result.isSuccess(memberResult)) {
            throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }
        AffiliateInfoDTO affiliateInfoDTO = memberResult.getData();
        String realName = affiliateInfoDTO.getRealName();

        if (StringUtils.hasText(realName) && !realName.equals(form.getRealName())) {
            throw new BizException(ResultCode.REAL_NAME_IS_NOT_THE_SAME);
        } else {
            affiliateFeignClient.modifyAffiliateRealName(affiliateInfoDTO.getId(), form.getRealName());
        }

        return this.save(AffiliateBankCard.builder()
                .affiliateId(affiliateId)
                .realName(form.getRealName())
                .bankId(form.getBankId())
                .cardNo(form.getCardNo())
                .build()
        );
    }

    private void checkBankCardIsExist(String cardNo) {
        this.lambdaQuery()
                .eq(AffiliateBankCard::getCardNo, cardNo)
                .oneOpt()
                .ifPresent(x -> {
                    throw new BizException(ResultCode.BANK_CARD_DUPLICATE);
                });
    }
}





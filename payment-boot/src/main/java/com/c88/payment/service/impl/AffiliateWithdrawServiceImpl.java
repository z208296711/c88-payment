package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.c88.affiliate.api.dto.AffiliateInfoDTO;
import com.c88.affiliate.api.feign.AffiliateFeignClient;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.payment.dto.AddAffiliateBalanceDTO;
import com.c88.payment.enums.AffiliateBalanceChangeTypeEnum;
import com.c88.payment.enums.MemberTypeEnum;
import com.c88.payment.pojo.entity.AffiliateBalance;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.AffiliateWithdrawApplyForm;
import com.c88.payment.service.IAffiliateBalanceService;
import com.c88.payment.service.IAffiliateWithdrawService;
import com.c88.payment.service.IMemberWithdrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.c88.common.core.constant.TopicConstants.WITHDRAW_APPLY;
import static com.c88.common.core.enums.WithdrawStateEnum.APPLY;
import static com.c88.common.core.enums.WithdrawStateEnum.SECOND;
import static com.c88.common.web.util.UUIDUtils.genWithdrawOrderId;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateWithdrawServiceImpl implements IAffiliateWithdrawService {

    private final IMemberWithdrawService iWithdrawService;

    private final IAffiliateBalanceService iAffiliateBalanceService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final PasswordEncoder passwordEncoder;

    private final AffiliateFeignClient affiliateFeignClient;

    @Override
    public Boolean withdrawApply(Long affiliateId, String ip, AffiliateWithdrawApplyForm form) {

        AffiliateBalance affiliateBalance = iAffiliateBalanceService.findByAffiliateId(affiliateId);
        if (affiliateBalance.getBalance().compareTo(form.getAmount()) < 0) {
            throw new BizException("home.alert001");// 會員餘額不足
        }
        Result<AffiliateInfoDTO> affiliateDTOResult = affiliateFeignClient.getAffiliateInfoById(affiliateId);
        if (!Result.isSuccess(affiliateDTOResult)) {
            throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }
        if (isEmpty(affiliateDTOResult.getData().getWithdrawPassword())) {
            throw new BizException("withdrawal.title009");// 設置提款密碼後才可提款
        }
        if (!passwordEncoder.matches(form.getWithdrawPassword(), affiliateDTOResult.getData().getWithdrawPassword())) {
            throw new BizException(ResultCode.WITHDRAW_PASSWORD_ERROR);
        }
        AffiliateInfoDTO affiliateInfoDTO = affiliateDTOResult.getData();
        if (iWithdrawService.getOne(new LambdaQueryWrapper<MemberWithdraw>()
                .eq(MemberWithdraw::getType, 1)
                .eq(MemberWithdraw::getUsername, affiliateInfoDTO.getUsername())
                .in(MemberWithdraw::getState, APPLY.getState(), SECOND.getState())
                .last("limit 1")) != null) {// 會員同一時間只能申請一個提款
            throw new BizException("error.alreadyWithdrawal");
        }

        String serialNo = genWithdrawOrderId();
        MemberWithdraw withdraw = new MemberWithdraw();
        withdraw.setAmount(form.getAmount());
        withdraw.setWithdrawNo(serialNo);
        withdraw.setUid(affiliateInfoDTO.getId());
        withdraw.setType(MemberTypeEnum.AGENT.getValue());
        withdraw.setApplyTime(LocalDateTime.now());
        withdraw.setApplyIp(ip);
        withdraw.setUsername(affiliateInfoDTO.getUsername());
        withdraw.setRealName(affiliateInfoDTO.getRealName());
        withdraw.setCurrentBalance(affiliateBalance.getBalance().subtract(form.getAmount()));
        withdraw.setBankId(form.getBankId());
        withdraw.setBankCardNo(form.getBankCardNo());
        withdraw.setCryptoName(form.getCryptoName());
        withdraw.setCryptoAddress(form.getCryptoAddress());
        boolean result = iWithdrawService.save(withdraw);
        if (result) {
            // 預先扣除用戶餘額
            iAffiliateBalanceService.addBalance(
                    AddAffiliateBalanceDTO.builder()
                            .serialNo(serialNo)
                            .type(AffiliateBalanceChangeTypeEnum.WITHDRAW.getValue())
                            .affiliateId(affiliateId)
                            .amount(form.getAmount().negate())
                            .gmtCreate(LocalDateTime.now())
                            .build()
            );
            kafkaTemplate.send(WITHDRAW_APPLY, withdraw);
        }
        return true;
    }

}

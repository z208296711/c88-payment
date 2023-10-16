package com.c88.payment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.affiliate.api.dto.AffiliateInfoDTO;
import com.c88.affiliate.api.dto.AffiliateMemberDTO;
import com.c88.affiliate.api.dto.AuthAffiliateDTO;
import com.c88.affiliate.api.dto.CheckAffiliateLowLevelDTO;
import com.c88.affiliate.api.feign.AffiliateFeignClient;
import com.c88.affiliate.api.feign.AffiliateMemberClient;
import com.c88.common.core.enums.BalanceChangeTypeLinkEnum;
import com.c88.common.core.enums.MemberStatusEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.core.util.DateUtil;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.UUIDUtils;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.AuthUserDTO;
import com.c88.payment.dto.AddAffiliateBalanceDTO;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.enums.AffiliateBalanceChangeTypeEnum;
import com.c88.payment.mapstruct.AffiliateMemberTransferRecordConverter;
import com.c88.payment.mapstruct.AffiliateTransferRecordConverter;
import com.c88.payment.pojo.entity.AffiliateBalance;
import com.c88.payment.pojo.entity.AffiliateMemberTransferRecord;
import com.c88.payment.pojo.entity.AffiliateTransferRecord;
import com.c88.payment.pojo.entity.MemberBalance;
import com.c88.payment.pojo.form.AdminSearchAffiliateMemberTransferForm;
import com.c88.payment.pojo.form.AdminSearchAffiliateTransferForm;
import com.c88.payment.pojo.form.AffiliateTransferForm;
import com.c88.payment.pojo.form.SearchAffiliateTransferForm;
import com.c88.payment.pojo.vo.AdminAffiliateMemberTransferRecordVO;
import com.c88.payment.pojo.vo.AdminAffiliateTransferRecordVO;
import com.c88.payment.pojo.vo.AffiliateMemberTransferRecordVO;
import com.c88.payment.pojo.vo.AffiliateTransferRecordVO;
import com.c88.payment.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.COMMISSION;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateTransferServiceImpl implements IAffiliateTransferService {

    private final IAffiliateTransferRecordService iAffiliateTransferRecordService;

    private final IAffiliateMemberTransferRecordService iAffiliateMemberTransferRecordService;

    private final IAffiliateBalanceService iAffiliateBalanceService;

    private final IMemberBalanceService iMemberBalanceService;

    private final AffiliateTransferRecordConverter affiliateTransferRecordConverter;

    private final AffiliateMemberTransferRecordConverter affiliateMemberTransferRecordConverter;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final PasswordEncoder passwordEncoder;

    private final AffiliateFeignClient affiliateFeignClient;

    private final AffiliateMemberClient affiliateMemberClient;

    private final MemberFeignClient memberFeignClient;

    @Override
    @Transactional
    public Boolean memberTransfer(Long affiliateId,
                                  String ip,
                                  AffiliateTransferForm form) {

        AffiliateBalance affiliateBalance = iAffiliateBalanceService.findByAffiliateId(affiliateId);
        if (affiliateBalance.getBalance().compareTo(form.getAmount()) < 0) {
            throw new BizException("home.alert001");// 會員餘額不足
        }

        Result<AuthUserDTO> result = memberFeignClient.getMemberByUserName(form.getUsername());
        if (Result.isSuccess(result)) {
            AuthUserDTO user = result.getData();
            log.info("user:{}", user);
            if(user.getUserName()==null){
                throw new BizException("be_Agent_list.alert01");
            }else if(user.getStatus()== MemberStatusEnum.FREEZE.getStatus()){
                throw new BizException(ResultCode.USER_ACCOUNT_LOCKED);
            }
        }

        Result<List<AffiliateMemberDTO>> result1 = affiliateMemberClient.findAffiliateMembersByParentUsername(affiliateBalance.getUsername());
        log.info("aff:{}", result1);
        if (Result.isSuccess(result1)) {
            List<String> userList = result1.getData().stream().map(AffiliateMemberDTO::getMemberUsername).collect(Collectors.toList());
            if(!userList.contains(form.getUsername())){
                throw new BizException(ResultCode.MEMBER_IS_NOT_BELONG_AFFILIATE);
            }
        }
//        CheckAffiliateLowLevelDTO checkAffiliateLowLevelDTO = new CheckAffiliateLowLevelDTO();
//        checkAffiliateLowLevelDTO.setParentUsername(affiliateBalance.getUsername());
//        checkAffiliateLowLevelDTO.setMemberUsername(form.getUsername());
//        Result<Boolean> affiliateLowLevelResult = affiliateFeignClient.isAffiliateLowLevel(checkAffiliateLowLevelDTO);
//        if (!Result.isSuccess(affiliateLowLevelResult) || Boolean.FALSE.equals(affiliateLowLevelResult.getData())) {
//            throw new BizException(ResultCode.MEMBER_IS_NOT_BELONG_AFFILIATE);
//        }

        //限額
        LocalDateTime[] todayFloorAndCelling = DateUtil.getTodayFloorAndCelling(new Date());
        BigDecimal totalBalacne = iAffiliateMemberTransferRecordService.lambdaQuery()
                .eq(AffiliateMemberTransferRecord::getAffiliateId, affiliateId)
                .ge(AffiliateMemberTransferRecord::getGmtCreate, todayFloorAndCelling[0])
                .lt(AffiliateMemberTransferRecord::getGmtCreate, todayFloorAndCelling[1]).list()
                .stream().map(AffiliateMemberTransferRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalBalacne = totalBalacne.add(form.getAmount());
        if(totalBalacne.compareTo(new BigDecimal(2000000))>0){
            throw new BizException(ResultCode.TRANSFER_OVER_LIMIT);
        }

        String serialNo = UUIDUtils.genOrderId("TF");

        MemberBalance memberBalance = iMemberBalanceService.findByUsername(form.getUsername());
        AffiliateMemberTransferRecord affiliateMemberTransferRecord = new AffiliateMemberTransferRecord();
        affiliateMemberTransferRecord.setSerialNo(serialNo);
        affiliateMemberTransferRecord.setAffiliateId(affiliateBalance.getAffiliateId());
        affiliateMemberTransferRecord.setAffiliateUsername(affiliateBalance.getUsername());
        affiliateMemberTransferRecord.setMemberId(memberBalance.getMemberId());
        affiliateMemberTransferRecord.setMemberUsername(memberBalance.getUsername());
        affiliateMemberTransferRecord.setAmount(form.getAmount());
        affiliateMemberTransferRecord.setNote(form.getNote());
        iAffiliateMemberTransferRecordService.save(affiliateMemberTransferRecord);

        iAffiliateBalanceService.addBalance(
                AddAffiliateBalanceDTO.builder()
                        .serialNo(serialNo)
                        .affiliateId(affiliateId)
                        .type(AffiliateBalanceChangeTypeEnum.MEMBER_TRANSFER.getValue())
                        .amount(form.getAmount().negate())
                        .gmtCreate(LocalDateTime.now())
                        .build()
        );

        iMemberBalanceService.addBalance(
                AddBalanceDTO.builder()
                        .memberId(memberBalance.getMemberId())
                        .balance(form.getAmount())
                        .type(COMMISSION.getType())
                        .betRate(BigDecimal.ONE)
                        .balanceChangeTypeLinkEnum(BalanceChangeTypeLinkEnum.COMMISSION)
                        .note(COMMISSION.getI18n())
                        .build()
        );


        return true;
    }

    @Override
    @Transactional
    public Boolean transfer(Long affiliateId, String ip, AffiliateTransferForm form) {

        AffiliateBalance affiliateBalance = iAffiliateBalanceService.findByAffiliateId(affiliateId);
        if (affiliateBalance.getBalance().compareTo(form.getAmount()) < 0) {
            throw new BizException("home.alert001");// 會員餘額不足
        }

        Result<AffiliateInfoDTO> targetAffiliateDTOResult = affiliateFeignClient.getAffiliateInfoByUsername(form.getUsername());
        if (!Result.isSuccess(targetAffiliateDTOResult)) {
            throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
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

        LocalDateTime[] todayFloorAndCelling = DateUtil.getTodayFloorAndCelling(new Date());
        //限額
        BigDecimal totalBalacne = iAffiliateTransferRecordService.lambdaQuery()
                .eq(AffiliateTransferRecord::getAffiliateId, affiliateId)
                .ge(AffiliateTransferRecord::getGmtCreate, todayFloorAndCelling[0])
                .lt(AffiliateTransferRecord::getGmtCreate, todayFloorAndCelling[1]).list()
                .stream().map(AffiliateTransferRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalBalacne = totalBalacne.add(form.getAmount());
        if(totalBalacne.compareTo(new BigDecimal(2000000))>0){
            throw new BizException(ResultCode.TRANSFER_OVER_LIMIT);
        }

        AffiliateInfoDTO targetAffiliateInfoDTO = targetAffiliateDTOResult.getData();

        AffiliateInfoDTO affiliateInfoDTO = affiliateDTOResult.getData();

        if (!targetAffiliateInfoDTO.getParents().startsWith(affiliateInfoDTO.getParents())) {
            throw new BizException(ResultCode.TARGET_IS_NOT_BELONG_THIS_AFFILIATE);
        }

        String serialNo = UUIDUtils.genOrderId("TF");

        AffiliateTransferRecord affiliateTransferRecord = new AffiliateTransferRecord();
        affiliateTransferRecord.setSerialNo(serialNo);
        affiliateTransferRecord.setAffiliateId(affiliateInfoDTO.getId());
        affiliateTransferRecord.setAffiliateUsername(affiliateInfoDTO.getUsername());
        affiliateTransferRecord.setTargetAffiliateId(targetAffiliateInfoDTO.getId());
        affiliateTransferRecord.setTargetAffiliateUsername(targetAffiliateInfoDTO.getUsername());
        affiliateTransferRecord.setAmount(form.getAmount());
        affiliateTransferRecord.setNote(form.getNote());
        iAffiliateTransferRecordService.save(affiliateTransferRecord);

        iAffiliateBalanceService.addBalance(
                AddAffiliateBalanceDTO.builder()
                        .affiliateId(affiliateId)
                        .serialNo(serialNo)
                        .type(AffiliateBalanceChangeTypeEnum.AFFILIATE_TRANSFER.getValue())
                        .amount(form.getAmount().negate())
                        .gmtCreate(LocalDateTime.now())
                        .build()
        );

        iAffiliateBalanceService.addBalance(
                AddAffiliateBalanceDTO.builder()
                        .type(AffiliateBalanceChangeTypeEnum.AFFILIATE_TRANSFER.getValue())
                        .affiliateId(targetAffiliateInfoDTO.getId())
                        .amount(form.getAmount())
                        .gmtCreate(LocalDateTime.now())
                        .build()
        );

        return true;
    }

    @Override
    public IPage<AffiliateTransferRecordVO> findTransferRecordPage(Long affiliateId, SearchAffiliateTransferForm form) {
        return iAffiliateTransferRecordService.lambdaQuery()
                .eq(AffiliateTransferRecord::getAffiliateId, affiliateId)
                .eq(StringUtils.isNotBlank(form.getUsername()), AffiliateTransferRecord::getTargetAffiliateUsername, form.getUsername())
                .ge(form.getStartTime() != null, AffiliateTransferRecord::getGmtCreate, LocalDateTime.of(form.getStartTime(), LocalTime.MIN).minusHours(7))
                .le(form.getEndTime()!= null, AffiliateTransferRecord::getGmtCreate, LocalDateTime.of(form.getEndTime(),LocalTime.MAX).minusHours(7))
                .orderByDesc(AffiliateTransferRecord::getGmtCreate)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(affiliateTransferRecordConverter::toVo);
    }

    @Override
    public IPage<AffiliateMemberTransferRecordVO> findMemberTransferRecordPage(Long affiliateId, SearchAffiliateTransferForm form) {
        return iAffiliateMemberTransferRecordService.lambdaQuery()
                .eq(AffiliateMemberTransferRecord::getAffiliateId, affiliateId)
                .eq(StringUtils.isNotBlank(form.getUsername()), AffiliateMemberTransferRecord::getMemberUsername, form.getUsername())
                .ge(form.getStartTime() != null, AffiliateMemberTransferRecord::getGmtCreate, LocalDateTime.of(form.getStartTime(), LocalTime.MIN).minusHours(7))
                .le(form.getEndTime() != null, AffiliateMemberTransferRecord::getGmtCreate, LocalDateTime.of(form.getEndTime(),LocalTime.MAX).minusHours(7))
                .orderByDesc(AffiliateMemberTransferRecord::getGmtCreate)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(affiliateMemberTransferRecordConverter::toVo);
    }

    @Override
    public IPage<AdminAffiliateTransferRecordVO> findAdminTransferRecordPage(AdminSearchAffiliateTransferForm form) {
        return iAffiliateTransferRecordService.lambdaQuery()
                .like(StringUtils.isNotBlank(form.getAffiliateUsername()), AffiliateTransferRecord::getAffiliateUsername, form.getAffiliateUsername())
                .like(StringUtils.isNotBlank(form.getTargetAffiliateUsername()), AffiliateTransferRecord::getTargetAffiliateUsername, form.getTargetAffiliateUsername())
                .ge(form.getStartTime() != null, AffiliateTransferRecord::getGmtCreate, form.getStartTime())
                .le(form.getEndTime()!= null, AffiliateTransferRecord::getGmtCreate, form.getEndTime())
                .orderByDesc(AffiliateTransferRecord::getGmtCreate)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(affiliateTransferRecordConverter::toAdminAffiliateTransferRecordVO);
    }

    @Override
    public IPage<AdminAffiliateMemberTransferRecordVO> findAdminMemberTransferRecordPage(AdminSearchAffiliateMemberTransferForm form) {
        return iAffiliateMemberTransferRecordService.lambdaQuery()
                .like(StringUtils.isNotBlank(form.getAffiliateUsername()), AffiliateMemberTransferRecord::getAffiliateUsername, form.getAffiliateUsername())
                .like(StringUtils.isNotBlank(form.getMemberUsername()), AffiliateMemberTransferRecord::getMemberUsername, form.getMemberUsername())
                .ge(form.getStartTime() != null, AffiliateMemberTransferRecord::getGmtCreate, form.getStartTime())
                .le(form.getEndTime()!= null, AffiliateMemberTransferRecord::getGmtCreate, form.getEndTime())
                .orderByDesc(AffiliateMemberTransferRecord::getGmtCreate)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(affiliateMemberTransferRecordConverter::toAdminAffiliateMemberTransferRecordVO);
    }


}

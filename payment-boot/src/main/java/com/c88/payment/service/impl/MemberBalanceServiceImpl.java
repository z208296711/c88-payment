package com.c88.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.affiliate.api.dto.AffiliateMemberDTO;
import com.c88.affiliate.api.feign.AffiliateFeignClient;
import com.c88.affiliate.api.feign.AffiliateMemberClient;
import com.c88.common.core.enums.BalanceChangeTypeLinkEnum;
import com.c88.common.core.enums.RemitStateEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.Pageutil;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.member.enums.RechargeAwardModeEnum;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.dto.MemberBalanceInfoDTO;
import com.c88.payment.dto.MemberFirstRechargeDTO;
import com.c88.payment.dto.PaymentMemberBalanceDTO;
import com.c88.payment.dto.RechargeBonusDTO;
import com.c88.payment.enums.BalanceChangeTypeEnum;
import com.c88.payment.mapper.MemberBalanceMapper;
import com.c88.payment.mapper.PaymentMemberBalanceMapper;
import com.c88.payment.mapper.WithdrawMapper;
import com.c88.payment.mapstruct.MemberBalanceConverter;
import com.c88.payment.pojo.entity.BalanceChangeRecord;
import com.c88.payment.pojo.entity.MemberBalance;
import com.c88.payment.pojo.entity.MemberWithdrawBet;
import com.c88.payment.pojo.form.RechargeWithdrawReportForm;
import com.c88.payment.pojo.vo.*;
import com.c88.payment.service.IBalanceChangeRecordService;
import com.c88.payment.service.IMemberBalanceService;
import com.c88.payment.service.IMemberRechargeService;
import com.c88.payment.service.IMemberWithdrawBetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.c88.common.core.constant.TopicConstants.MEMBER_FIRST_RECHARGE;
import static com.c88.common.core.constant.TopicConstants.RECHARGE_BONUS;

/**
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberBalanceServiceImpl extends ServiceImpl<MemberBalanceMapper, MemberBalance> implements IMemberBalanceService {

    private final MemberFeignClient memberFeignClient;

    private final IMemberWithdrawBetService iMemberWithdrawBetService;

    private final IBalanceChangeRecordService iBalanceChangeRecordService;

    private final MemberBalanceConverter memberBalanceConverter;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final PaymentMemberBalanceMapper paymentMemberBalanceMapper;

    private final IMemberRechargeService rechargeService;

    private final WithdrawMapper withdrawMapper;

    private final AffiliateMemberClient affiliateMemberClient;

    @Override
    public MemberBalance findByMemberId(Long memberId) {
        return this.lambdaQuery()
                .eq(MemberBalance::getMemberId, memberId)
                .oneOpt()
                .orElseGet(() -> {
                            Result<MemberInfoDTO> memberResult = memberFeignClient.getMemberInfo(memberId);
                            if (!Result.isSuccess(memberResult)) {
                                throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
                            }
                            MemberInfoDTO memberInfoDTO = memberResult.getData();
                            MemberBalance memberBalance = MemberBalance.builder()
                                    .memberId(memberInfoDTO.getId())
                                    .username(memberInfoDTO.getUsername())
                                    .balance(BigDecimal.ZERO)
                                    .build();
                            this.save(memberBalance);
                            return memberBalance;
                        }
                );
    }

    @Override
    public MemberBalance findByUsername(String username) {
        return this.lambdaQuery()
                .eq(MemberBalance::getUsername, username)
                .oneOpt()
                .orElseGet(() -> {
                            Result<MemberInfoDTO> memberResult = memberFeignClient.getMemberInfo(username);
                            if (!Result.isSuccess(memberResult)) {
                                throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
                            }
                            MemberInfoDTO memberInfoDTO = memberResult.getData();
                            MemberBalance memberBalance = MemberBalance.builder()
                                    .memberId(memberInfoDTO.getId())
                                    .username(memberInfoDTO.getUsername())
                                    .balance(BigDecimal.ZERO)
                                    .build();
                            this.save(memberBalance);
                            return memberBalance;
                        }
                );
    }

    @Override
    public PaymentMemberBalanceDTO findByMemberIdDTO(Long memberId) {
        return this.lambdaQuery()
                .eq(MemberBalance::getMemberId, memberId)
                .oneOpt()
                .map(memberBalanceConverter::toDTO)
                .orElseGet(() -> {
                            Result<MemberInfoDTO> memberResult = memberFeignClient.getMemberInfo(memberId);
                            if (!Result.isSuccess(memberResult)) {
                                throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
                            }
                            MemberInfoDTO memberInfoDTO = memberResult.getData();
                            MemberBalance memberBalance = MemberBalance.builder()
                                    .memberId(memberInfoDTO.getId())
                                    .username(memberInfoDTO.getUsername())
                                    .balance(BigDecimal.ZERO)
                                    .build();
                            this.save(memberBalance);
                            return memberBalanceConverter.toDTO(memberBalance);
                        }
                );
    }

    @Override
    public PaymentMemberBalanceDTO findByUsernameDTO(String username) {
        return this.lambdaQuery()
                .eq(MemberBalance::getUsername, username)
                .oneOpt()
                .map(memberBalanceConverter::toDTO)
                .orElseGet(() -> {
                            Result<MemberInfoDTO> memberResult = memberFeignClient.getMemberInfo(username);
                            if (!Result.isSuccess(memberResult)) {
                                throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
                            }
                            MemberInfoDTO memberInfoDTO = memberResult.getData();
                            MemberBalance memberBalance = MemberBalance.builder()
                                    .memberId(memberInfoDTO.getId())
                                    .username(memberInfoDTO.getUsername())
                                    .balance(BigDecimal.ZERO)
                                    .build();
                            this.save(memberBalance);
                            return memberBalanceConverter.toDTO(memberBalance);
                        }
                );
    }

    @Override
    @Transactional
    public BigDecimal addBalance(AddBalanceDTO addBalanceDTO) {
        log.info("addBalance: {}", JSON.toJSONString(addBalanceDTO));
        MemberBalance memberBalance = this.findByMemberId(addBalanceDTO.getMemberId());
        BigDecimal beforeBalance = memberBalance.getBalance();
        BigDecimal afterBalance = memberBalance.getBalance().add(addBalanceDTO.getBalance());
        this.updateById(memberBalance);

        BalanceChangeRecord record = new BalanceChangeRecord();
        record.setMemberId(addBalanceDTO.getMemberId());
        record.setUsername(memberBalance.getUsername());
        record.setType(addBalanceDTO.getType());
        record.setNote(addBalanceDTO.getNote());
        record.setAmount(addBalanceDTO.getBalance());
        record.setBeforeBalance(beforeBalance);
        record.setCurrentBalance(afterBalance);
        record.setBetRate(addBalanceDTO.getBetRate());
        record.setGmtCreate(addBalanceDTO.getGmtCreate());
        record.setNeedBet(addBalanceDTO.getBalance().multiply(addBalanceDTO.getBetRate()));

        MemberWithdrawBet memberWithdrawBet = iMemberWithdrawBetService.findMemberWithdrawBet(record.getMemberId(), record.getUsername());

        BalanceChangeTypeEnum balanceChangeType = BalanceChangeTypeEnum.fromIntValue(addBalanceDTO.getType());
        AtomicReference<BigDecimal> rechargeAwardFullOut = new AtomicReference<>(BigDecimal.ZERO);
        switch (balanceChangeType) {
            case RECHARGE:
                //若有存送優惠,需充值處理紀錄
                Optional.ofNullable(addBalanceDTO.getRechargeAwardDTO())
                        .ifPresentOrElse(rechargeAwardDTO -> {
                            BigDecimal amount = addBalanceDTO.getBalance();
                            if (rechargeAwardDTO.getMode().equals(RechargeAwardModeEnum.RATE.getCode())) {
                                // 最高贈送金額
                                BigDecimal maxAmount = rechargeAwardDTO.getMaxAwardAmount().divide(rechargeAwardDTO.getRate(), 2, RoundingMode.HALF_UP);
                                rechargeAwardFullOut.set(amount.compareTo(maxAmount) < 0 ?
                                        amount :
                                        maxAmount);
                            } else {
                                rechargeAwardFullOut.set(rechargeAwardDTO.getMinJoinAmount());
                            }
                            BigDecimal needBet = rechargeAwardFullOut.get().negate().add(amount).multiply(addBalanceDTO.getBetRate());
                            record.setRechargeAwardFullOut(rechargeAwardFullOut.get().negate());
                            record.setNeedBet(needBet);

                            memberWithdrawBet.setAccBet(memberWithdrawBet.getAccBet().add(needBet));
                        }, () -> memberWithdrawBet.setAccBet(memberWithdrawBet.getAccBet().add(record.getNeedBet())));

                // 沒有首次充值時間時寫入首次充值時間
                if (Objects.isNull(memberBalance.getFirstRechargeTime())) {
                    memberBalance.setFirstRechargeTime(LocalDateTime.now());
                    memberBalance.setFirstRechargeBalance(addBalanceDTO.getBalance());

                    // 送出首次充值訊息
                    kafkaTemplate.send(MEMBER_FIRST_RECHARGE,
                            MemberFirstRechargeDTO.builder()
                                    .orderId(addBalanceDTO.getOrderId())
                                    .build()
                    );
                }

                // 寫入最後充值時間
                memberBalance.setLastRechargeTime(LocalDateTime.now());
                break;
            case BONUS:
                memberWithdrawBet.setAccBet(memberWithdrawBet.getAccBet().add(record.getNeedBet()));
                break;
            case ZERO:
                record.setValidBet(memberWithdrawBet.getValidBet().negate());
                record.setAccBet(BigDecimal.ZERO);
                record.setBetRate(null);
                memberWithdrawBet.setValidBet(BigDecimal.ZERO);
                memberWithdrawBet.setAccBet(BigDecimal.ZERO);
                break;
            case COMMISSION:
                memberWithdrawBet.setAccBet(memberWithdrawBet.getAccBet().add(record.getNeedBet()));
                break;
            case ADJUST:
                record.setNeedBet(BigDecimal.ZERO);
                record.setBetRate(null);
                break;
            case TRANSFER:
                record.setNeedBet(BigDecimal.ZERO);
                record.setBetRate(null);
                break;
            case WITHDRAW:
                record.setAmount(addBalanceDTO.getBalance().negate());
                record.setNeedBet(BigDecimal.ZERO);
                record.setBetRate(null);

                // 沒有首次充值時間時寫入首次提款時間
                if (Objects.isNull(memberBalance.getFirstWithdrawTime())) {
                    memberBalance.setFirstWithdrawTime(LocalDateTime.now());
                    memberBalance.setFirstWithdrawBalance(addBalanceDTO.getBalance());
                }

                // 寫入最後充值時間
                memberBalance.setLastWithdrawTime(LocalDateTime.now());
                break;
            case RECHARGE_PROMOTION:
                break;
        }

        iMemberWithdrawBetService.updateById(memberWithdrawBet);

        record.setValidBet(memberWithdrawBet.getValidBet());
        record.setAccBet(memberWithdrawBet.getAccBet());
        iBalanceChangeRecordService.save(record);
        memberBalance.setBalance(afterBalance);

        this.updateById(memberBalance);

        //新增存送優惠log
        AtomicReference<BigDecimal> rechargeAwardAmount = new AtomicReference<>(BigDecimal.ZERO);
        Optional.ofNullable(addBalanceDTO.getRechargeAwardDTO())
                .ifPresent(rechargeAwardDTO -> {

                    MemberWithdrawBet newMemberWithdrawBet = iMemberWithdrawBetService.findMemberWithdrawBet(record.getMemberId(), record.getUsername());

                    BigDecimal amount;
                    if (rechargeAwardDTO.getMode().equals(RechargeAwardModeEnum.RATE.getCode())) {
                        BigDecimal bonusAmount = addBalanceDTO.getBalance().multiply(rechargeAwardDTO.getRate());
                        amount = bonusAmount.compareTo(rechargeAwardDTO.getMaxAwardAmount()) > 0 ?
                                rechargeAwardDTO.getMaxAwardAmount() :
                                bonusAmount;
                    } else {
                        amount = rechargeAwardDTO.getFixAmount();
                    }

                    MemberBalance newBalance = this.findByMemberId(addBalanceDTO.getMemberId());
                    BigDecimal beforeNewBalance = memberBalance.getBalance();
                    BigDecimal afterNewBalance = memberBalance.getBalance().add(amount);
                    newBalance.setBalance(afterNewBalance);
                    this.updateById(newBalance);

                    BigDecimal needBet = rechargeAwardFullOut.get().add(amount).multiply(rechargeAwardDTO.getBetRate());
                    newMemberWithdrawBet.setAccBet(newMemberWithdrawBet.getAccBet().add(needBet));
                    BalanceChangeRecord rechargeAward = new BalanceChangeRecord();
                    rechargeAward.setMemberId(memberBalance.getMemberId());
                    rechargeAward.setUsername(memberBalance.getUsername());
                    rechargeAward.setType(BalanceChangeTypeLinkEnum.RECHARGE_PROMOTION.getType());
                    rechargeAward.setNote(rechargeAwardDTO.getName());
                    rechargeAward.setAmount(amount);
                    rechargeAward.setBeforeBalance(beforeNewBalance);
                    rechargeAward.setCurrentBalance(afterNewBalance);
                    rechargeAward.setBetRate(rechargeAwardDTO.getBetRate());
                    rechargeAward.setNeedBet(needBet);
                    rechargeAward.setRechargeAwardFullOut(rechargeAwardFullOut.get());
                    rechargeAward.setValidBet(newMemberWithdrawBet.getValidBet());
                    rechargeAward.setAccBet(newMemberWithdrawBet.getAccBet());
                    iBalanceChangeRecordService.save(rechargeAward);
                    memberBalance.setBalance(newBalance.getBalance());
                    iMemberWithdrawBetService.updateById(newMemberWithdrawBet);

                    rechargeAwardAmount.set(amount);
                });

        // 丟出優惠訊息
        if (addBalanceDTO.getBalanceChangeTypeLinkEnum() != null && addBalanceDTO.getBalanceChangeTypeLinkEnum().getType() == 3) {
            kafkaTemplate.send(RECHARGE_BONUS,
                    RechargeBonusDTO.builder()
                            .memberId(addBalanceDTO.getMemberId())
                            .username(memberBalance.getUsername())
                            .balanceChangeTypeLinkEnum(addBalanceDTO.getBalanceChangeTypeLinkEnum())
                            .amount(addBalanceDTO.getBalance())
                            .betRate(addBalanceDTO.getBetRate())
                            .gmtCreate(addBalanceDTO.getGmtCreate())
                            .reviewUsername(addBalanceDTO.getBonusReviewUsername())
                            .note(addBalanceDTO.getNote())
                            .build()
            );
        }

        return memberBalance.getBalance();
    }

    @Transactional
    @Override
    public List<PaymentMemberBalanceDTO> findByMemberIdArrayDTO(List<Long> memberIds) {
        // 取得清單上的會員餘額
        List<MemberBalance> memberBalances = this.lambdaQuery()
                .in(MemberBalance::getMemberId, memberIds)
                .list();

        // 實際取得的會員數量需與清單上的會員數量一致，否則需新增新的會員餘額
        if (memberBalances.size() != memberIds.size()) {
            // 取得沒有匹配到的會員ID
            List<Long> memberBalanceIds = memberIds.stream()
                    .map(memberId -> memberBalances.stream().anyMatch(memberBalance -> Objects.equals(memberBalance.getMemberId(), memberId)) ? null : memberId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<MemberBalance> memberBalanceEntity = memberBalanceIds.stream()
                    .map(memberBalanceId -> {
                                Result<MemberInfoDTO> memberResult = memberFeignClient.getMemberInfo(memberBalanceId);
                                if (!Result.isSuccess(memberResult)) {
                                    throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
                                }
                                MemberInfoDTO memberInfoDTO = memberResult.getData();
                                return MemberBalance.builder()
                                        .memberId(memberInfoDTO.getId())
                                        .username(memberInfoDTO.getUsername())
                                        .balance(BigDecimal.ZERO)
                                        .build();
                            }
                    )
                    .collect(Collectors.toList());

            // 新增會員餘額實體
            this.saveBatch(memberBalanceEntity);

            memberBalances.addAll(memberBalanceEntity);
        }

        return memberBalanceConverter.toDTO(memberBalances);
    }

    @Override
    public List<MemberBalanceInfoDTO> findMemberBalanceInfoByIds(List<Long> memberIds) {
        if (CollectionUtils.isEmpty(memberIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery()
                .in(MemberBalance::getMemberId, memberIds)
                .list()
                .stream()
                .map(memberBalanceConverter::toMemberInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberBalanceInfoDTO> findMemberBalanceInfoByUsernames(List<String> usernames) {
        if (CollectionUtils.isEmpty(usernames)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery()
                .in(MemberBalance::getUsername, usernames)
                .list()
                .stream()
                .map(memberBalanceConverter::toMemberInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PageSummaryVO getRechargeWithdrawReport(RechargeWithdrawReportForm form) {
        QueryWrapper<RechargeWithdrawReportVO> queryWrapper = new QueryWrapper<RechargeWithdrawReportVO>();
        // "where record.gmt_modified>#{startTime} and record.gmt_modified<#{endTime} GROUP BY member.user_name")
        queryWrapper.ge("record.gmt_modified", form.getStartTime());
        queryWrapper.lt("record.gmt_modified", form.getEndTime());
        queryWrapper.eq(StringUtils.isNotEmpty(form.getUsername()), "member.user_name", form.getUsername());
        queryWrapper.and(Wrapper -> Wrapper.eq("record.type", 1).or().eq("record.type", 2));
        queryWrapper.groupBy("member.user_name");
        List<RechargeWithdrawReportVO> basicInfoList = paymentMemberBalanceMapper.getBasicInfo(queryWrapper);
//        List<RechargeWithdrawReportVO> basicInfo = basicInfoPage.getRecords();

        List<Integer> typeList = new ArrayList<>();
        typeList.add(0); //在線充值
        typeList.add(1); //手動充值
        typeList.add(2);
        typeList.add(3);
        Map<String, RechargeWithdrawReportVO> rechargeMap = rechargeService.getSuccessRechargeCount(1, typeList, form.getStartTime(), form.getEndTime())
                .stream().collect(Collectors.toMap(RechargeWithdrawReportVO::getMemberUsername, Function.identity()));
        Map<String, RechargeWithdrawReportVO> withdrawMap = withdrawMapper.getSuccessWithdrawCount(RemitStateEnum.SUCCESS.getValue().intValue(), RemitStateEnum.PAY_SUCCESS.getValue().intValue(), form.getStartTime(), form.getEndTime())
                .stream().collect(Collectors.toMap(RechargeWithdrawReportVO::getMemberUsername, Function.identity()));

        //找上級名稱
        List<Long> memberIds = basicInfoList.stream().mapToLong(RechargeWithdrawReportVO::getMemberId).boxed().collect(Collectors.toList());

        List<AffiliateMemberDTO> memberDTOS = memberIds.size()>0 ? affiliateMemberClient.findAffiliateMembers(memberIds).getData() : new ArrayList<>();
        Map<String, String> parentMap = memberDTOS.size()>0 ? memberDTOS.stream().collect(Collectors.toMap(AffiliateMemberDTO::getMemberUsername, AffiliateMemberDTO::getParentUsername)) : new HashMap<>();

        basicInfoList.forEach(vo -> {
            vo.setParentUsername(parentMap.get(vo.getMemberUsername()));

            RechargeWithdrawReportVO rechargeVO = rechargeMap.get(vo.getMemberUsername());
            vo.setRechargeCount(rechargeVO == null ? 0 : rechargeVO.getRechargeCount());
            vo.setRechargeTotalAmount(rechargeVO == null ? BigDecimal.ZERO : rechargeVO.getRechargeTotalAmount());
            vo.setRechargeFee(rechargeVO == null ? BigDecimal.ZERO : rechargeVO.getRechargeFee());

            RechargeWithdrawReportVO withdrawVO = withdrawMap.get(vo.getMemberUsername());
            vo.setWithdrawFee(BigDecimal.ZERO);
            vo.setWithdrawCount(withdrawVO == null ? 0 : withdrawVO.getWithdrawCount());
            vo.setWithdrawTotalAmount(withdrawVO == null ? BigDecimal.ZERO : withdrawVO.getWithdrawTotalAmount());
        });
        basicInfoList.removeIf(vo-> vo.getRechargeTotalAmount().equals(BigDecimal.ZERO) && vo.getWithdrawTotalAmount().equals(BigDecimal.ZERO));

        BigDecimal mainCoin = basicInfoList.stream().map(RechargeWithdrawReportVO::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer rechargeCount = basicInfoList.stream().map(RechargeWithdrawReportVO::getRechargeCount).reduce(0, Integer::sum);
        BigDecimal rechargeAmount = basicInfoList.stream().map(RechargeWithdrawReportVO::getRechargeTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rechargeFee = basicInfoList.stream().map(RechargeWithdrawReportVO::getRechargeFee).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer withdrawCount = basicInfoList.stream().map(RechargeWithdrawReportVO::getWithdrawCount).reduce(0, Integer::sum);
        BigDecimal withdrawAmount = basicInfoList.stream().map(RechargeWithdrawReportVO::getWithdrawTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        RechargeWithdrawTotalVO totalVO = RechargeWithdrawTotalVO.builder()
                .rechargeAmount(rechargeAmount)
                .rechargeCount(rechargeCount)
                .rechargeFee(rechargeFee)
                .mainCoin(mainCoin)
                .withdrawAmount(withdrawAmount)
                .withdrawCount(withdrawCount)
                .withdrawFee(BigDecimal.ZERO)
                .totalFee(rechargeFee.subtract(BigDecimal.ZERO))
                .build();

//        basicInfoPage.setRecords(basicInfo);
        Pageable pageable = PageRequest.of(form.getPageNum()-1, form.getPageSize());
        org.springframework.data.domain.Page<RechargeWithdrawReportVO> basicInfoPage = Pageutil.createPageFromList(basicInfoList, pageable);

        IPage<RechargeWithdrawReportVO> page = new Page<RechargeWithdrawReportVO>();
        page.setRecords(basicInfoPage.getContent());
        page.setTotal(basicInfoList.size());
        page.setCurrent(form.getPageNum());
        page.setSize(form.getPageSize());

        PageSummaryVO pageSummaryVO = new PageSummaryVO();
        pageSummaryVO.setPage(page);
        pageSummaryVO.setObject(totalVO);
        return pageSummaryVO;
    }

    @Override
    public CompanyReportVO getFirstRechargeCountAndAmount(String startTime, String endTime) {
        List<MemberBalance> memberBalances = this.getBaseMapper().selectList(new LambdaQueryWrapper<MemberBalance>()
                .gt(MemberBalance::getFirstRechargeTime, startTime).lt(MemberBalance::getFirstRechargeTime, endTime));
        BigDecimal amount = memberBalances.stream().map(MemberBalance::getFirstRechargeBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        return CompanyReportVO.builder().firstRechargeCount(memberBalances.size()).firstRechargeAmount(amount).build();
    }

    @Override
    public RechargeWithdrawAwardCountVO getRechargeAwardCountAndAmount(String startTime, String endTime) {
        return paymentMemberBalanceMapper.getRechargeWithdrawAwardCountAndAmount(startTime, endTime);
    }

    public RechargeWithdrawAwardCountVO getWithdrawCountAndAmount(String startTime, String endTime) {
        return withdrawMapper.getSuccessWithdraw(RemitStateEnum.SUCCESS.getValue().intValue(), RemitStateEnum.PAY_SUCCESS.getValue().intValue(), startTime, endTime);
    }
}





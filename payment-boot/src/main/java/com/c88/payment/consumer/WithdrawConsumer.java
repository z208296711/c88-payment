package com.c88.payment.consumer;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.c88.admin.api.RiskConfigFeignClient;
import com.c88.admin.dto.RiskConfigDTO;
import com.c88.affiliate.api.dto.AffiliateInfoDTO;
import com.c88.affiliate.api.feign.AffiliateFeignClient;
import com.c88.common.core.enums.RiskTypeEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.util.DateUtil;
import com.c88.common.core.vo.BetRecordVo;
import com.c88.feign.RiskFeignClient;
import com.c88.game.adapter.api.GameFeignClient;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.MemberAssociationRateDTO;
import com.c88.member.dto.MemberDTO;
import com.c88.payment.enums.MemberTypeEnum;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.entity.WithdrawRisk;
import com.c88.payment.service.IBalanceChangeRecordService;
import com.c88.payment.service.IMemberRechargeService;
import com.c88.payment.service.IMemberWithdrawService;
import com.c88.payment.service.IWithdrawRiskService;
import com.c88.payment.vo.WithdrawVO;
import de.cronn.reflection.util.PropertyUtils;
import de.cronn.reflection.util.TypedPropertyGetter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.c88.common.core.constant.TopicConstants.ASSOCIATION_CONFIG;
import static com.c88.common.core.constant.TopicConstants.RISK_CONFIG;
import static com.c88.common.core.constant.TopicConstants.WITHDRAW;
import static com.c88.common.core.constant.TopicConstants.WITHDRAW_APPLY;
import static com.c88.common.core.enums.RemitStateEnum.PAY_FAILED;
import static com.c88.common.core.enums.RemitStateEnum.PAY_SUCCESS;
import static com.c88.common.core.enums.RemitStateEnum.SUCCESS;
import static com.c88.common.core.enums.WithdrawStateEnum.APPROVED;
import static com.c88.payment.constants.Constants.SYSTEM;
import static java.math.BigDecimal.ZERO;

@Slf4j
@Configuration
@AllArgsConstructor
public class WithdrawConsumer {

    private final IMemberWithdrawService iWithdrawService;
    private final RiskConfigFeignClient riskConfigFeignClient;
    private final IWithdrawRiskService iWithdrawRiskService;
    private final RiskFeignClient riskFeignClient;
    private final MemberFeignClient memberFeignClient;// hystrix properties is not initialized while calling feign client from @PostConstruct method
    private final GameFeignClient gameFeignClient;
    private final IMemberRechargeService iMemberRechargeService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static RiskConfigDTO localRiskConfig;
    private static MemberAssociationRateDTO systemAssociationRate;

    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final IBalanceChangeRecordService balanceChangeRecordService;

    private final AffiliateFeignClient affiliateFeignClient;

    @PostConstruct
    public void init() {
        try {
//            localRiskConfig = riskConfigFeignClient.getRiskConfig().getData();
            systemAssociationRate = memberFeignClient.getAssociation().getData();
        } catch (Exception e) {
            log.warn("fail to get risk config:" + e.getMessage());
        }
    }

    private BigDecimal getThreshold() {
        if (systemAssociationRate == null) {
            init();
        }
        return systemAssociationRate.getThreshold();
    }

    @KafkaListener(id = "#{T(java.util.UUID).randomUUID().toString()}", topics = RISK_CONFIG)
    public void listenRiskConfig(ConsumerRecord<String, RiskConfigDTO> record, Acknowledgment acknowledgment) {
        acknowledgment.acknowledge();
        localRiskConfig = record.value();

    }

    @KafkaListener(id = "#{T(java.util.UUID).randomUUID().toString()}", topics = ASSOCIATION_CONFIG)
    public void listenAssociationConfig(ConsumerRecord<String, MemberAssociationRateDTO> record, Acknowledgment acknowledgment) {
        acknowledgment.acknowledge();
        systemAssociationRate = record.value();
    }

    @KafkaListener(id = "withdraw", topics = WITHDRAW)
    public void listenWithdraw(ConsumerRecord<String, WithdrawVO> record, Acknowledgment acknowledgment) {
        try {
            WithdrawVO withdrawVO = record.value();
            MemberWithdraw withdraw = iWithdrawService.getById(withdrawVO.getId());
            if (withdraw.getBankId() != null) {// 目前三方代付只有銀行卡，尚無虛擬貨幣
                Merchant merchant = iWithdrawService.checkSuitableMerchant(withdraw);
                if (merchant != null) {
                    if (iWithdrawService.executeMerchantPay(withdraw, merchant, withdrawVO.getUserIp())) {
                        iWithdrawService.checkPayState(withdrawVO.getId());
                        MemberWithdraw afWithdraw = iWithdrawService.getById(withdrawVO.getId());
                        if (afWithdraw.getRemitState() != PAY_SUCCESS.getState() &&
                                afWithdraw.getRemitState() != PAY_FAILED.getState()) {
                            executorService.schedule(() -> {// 若訂單狀態尚未更新，兩分鐘後檢查一次
                                iWithdrawService.checkPayState(withdrawVO.getId());
                                MemberWithdraw afterWithdraw = iWithdrawService.getById(withdrawVO.getId());
                                if (afterWithdraw.getRemitState() != PAY_SUCCESS.getState() &&
                                        afterWithdraw.getRemitState() != PAY_FAILED.getState()) {// 若訂單狀態尚未更新，一分鐘後再檢查一次
                                    executorService.schedule(() -> {
                                        iWithdrawService.checkPayState(withdrawVO.getId());
                                    }, 60, TimeUnit.SECONDS);
                                }
                            }, 120, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("kafka withdraw fail exception: {}", ExceptionUtil.stacktraceToString(e));
        } finally {
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(id = "withdrawApply", topics = WITHDRAW_APPLY)
    public void listenWithdrawApply(ConsumerRecord<String, MemberWithdraw> record, Acknowledgment acknowledgment) {
        acknowledgment.acknowledge();
        Set<WithdrawRisk> set = new HashSet<>();
        MemberWithdraw withdraw = record.value();
        Integer type = withdraw.getType();
        localRiskConfig = riskConfigFeignClient.getRiskConfigByType(type).getData();
        boolean hasError = false;
        try {
            if (type.equals(MemberTypeEnum.MEMBER.getValue())) {
                validRiskForMember(set, withdraw);
            } else {
                validRiskForAgent(set, withdraw);
            }
        } catch (Exception e) {
            hasError = true;
            log.error("fail to check risk with withdraw id(" + withdraw.getId() + "):" + e.getMessage(), e);
        } finally {
            if (!set.isEmpty()) {
                iWithdrawRiskService.saveBatchIgnore(set);
            } else if (!hasError && localRiskConfig.getIsAuto()) {// 自動審單啟用時，若風控維度無異常，直接通過
                boolean result = iWithdrawService.update(new LambdaUpdateWrapper<MemberWithdraw>()
                        .eq(MemberWithdraw::getId, withdraw.getId())
                        .set(MemberWithdraw::getState, APPROVED.getState())
                        .set(MemberWithdraw::getFirstTime, LocalDateTime.now())
                        .set(MemberWithdraw::getFirstNote, "自動審單通過")
                        .set(MemberWithdraw::getFirstUser, SYSTEM));
                if (result) {
                    com.c88.payment.vo.WithdrawVO withdrawVO = new com.c88.payment.vo.WithdrawVO();
                    withdrawVO.setId(withdraw.getId());
                    withdrawVO.setUsername(withdraw.getUsername());
                    withdrawVO.setFinishedTime(withdraw.getSecondTime() != null ? withdraw.getSecondTime() :
                            (withdraw.getFirstTime() != null ? withdraw.getFirstTime() : LocalDateTime.now()));
                    withdrawVO.setUserIp(withdraw.getApplyIp());
                    kafkaTemplate.send(WITHDRAW, withdrawVO.getUsername(), withdrawVO);
                }
            }
        }
    }

    private void validRiskForMember(Set<WithdrawRisk> set, MemberWithdraw withdraw) {
        BetRecordVo betRecordVo = riskFeignClient.betRecord(withdraw.getUsername(), false).getData();
        LocalDateTime lastWithdrawTime = betRecordVo != null ? betRecordVo.getLastWithdraw() : null;
        BigDecimal totalRechargeFromLastWithdraw = iMemberRechargeService.memberTotalRecharge(withdraw.getUid(), lastWithdrawTime);
        if (totalRechargeFromLastWithdraw.compareTo(ZERO) != 0 && // 上次提款至這次提款的時間區間內所有充值金額為0，充提比就是0，不會警示充提比異常
                withdraw.getAmount().divide(totalRechargeFromLastWithdraw, 5, RoundingMode.HALF_UP).doubleValue() * 100 >= localRiskConfig.getWithdrawRechargeRatio()) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.WITHDRAW_RECHARGE_RATIO));
        }
        if (betRecordVo != null && betRecordVo.getValidBetAmount() != null &&
                betRecordVo.getValidBetAmount().compareTo(ZERO) != 0 &&
                localRiskConfig.getRewardBet() <= betRecordVo.getSettle().divide(betRecordVo.getValidBetAmount(), 5, RoundingMode.HALF_UP).doubleValue()) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.REWARD_BET));
        }
        if (localRiskConfig.getIsFirstWithdraw() &&
                iWithdrawService.count(new LambdaQueryWrapper<MemberWithdraw>().eq(MemberWithdraw::getUid, withdraw.getUid())
                        .eq(MemberWithdraw::getState, APPROVED.getState())
                        .in(MemberWithdraw::getRemitState, SUCCESS.getState(), PAY_SUCCESS.getState())) == 0) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.FIRST_WITHDRAW));
        }
        MemberDTO member;
        if (localRiskConfig.getIsFirstWithdrawAfterUpdated() &&
                (member = memberFeignClient.getMemberById(withdraw.getUid()).getData()) != null &&
                member.getLastInfoModified() != null &&
                iWithdrawService.count(new LambdaQueryWrapper<MemberWithdraw>().eq(MemberWithdraw::getUid, withdraw.getUid())
                        .gt(MemberWithdraw::getApplyTime, member.getLastInfoModified())
                        .lt(MemberWithdraw::getApplyTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(withdraw.getApplyTime()))) == 0) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.FIRST_WITHDRAW_AFTER_UPDATED));
        }
        if (withdraw.getAmount().doubleValue() >= localRiskConfig.getHugeWithdraw()) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.HUGE_WITHDRAW));
        }
        Set<Integer> tagIds = memberFeignClient.getMemberTagIds(withdraw.getUid()).getData();
        if (tagIds != null && localRiskConfig.getTagIds() != null &&
                Arrays.asList(localRiskConfig.getTagIds()).parallelStream()
                        .anyMatch(riskTagId -> tagIds.parallelStream().anyMatch(tagId -> Objects.equals(tagId, riskTagId)))) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.TAG));
        }
        if (localRiskConfig.getIsRelate() &&
                getThreshold().compareTo(
                        calculateAssociationScore(withdraw.getUid(), withdraw.getUsername(), withdraw.getApplyIp())) < 0) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.RELATE));
        }
        if (localRiskConfig.getIsBalanceAbnormal()) {
            BetRecordVo totalBetRecordVo = riskFeignClient.betRecord(withdraw.getUsername(), true).getData();
            BigDecimal totalWinLoss = totalBetRecordVo != null ? totalBetRecordVo.getWinLoss() : ZERO;
//                BigDecimal totalRecharge = iMemberRechargeService.memberTotalRecharge(withdraw.getUid(), null);
            BigDecimal totalRecharge = balanceChangeRecordService.getSumForRiskAbnormal(withdraw.getUid());
            BigDecimal totalWithdraw = iWithdrawService.memberTotalWithdraw(withdraw.getUid());
            if (totalRecharge.add(totalWinLoss).compareTo(totalWithdraw.add(withdraw.getAmount())
                    .add(gameFeignClient.findAllBalance(withdraw.getUid(), withdraw.getUsername()).getData())
                    .add(gameFeignClient.findMemberNonSettleBetAmount(withdraw.getUid()).getData())) != 0) {
                set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.BALANCE_ABNORMAL));
            }
        }
        Date date = new Date();
        LocalDateTime[] todayFloorAndCelling = DateUtil.getTodayFloorAndCelling(date);
        log.info("TodayTimeRange:{},{}", todayFloorAndCelling[0], todayFloorAndCelling[1]);
        BigDecimal totalRecharge = iMemberRechargeService.memberTotalRecharge(withdraw.getUid(), todayFloorAndCelling[0], todayFloorAndCelling[1]);
        if (withdraw.getAmount().subtract(totalRecharge).intValue() >= localRiskConfig.getEarnings()) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.EARNINGS));
        }
        if (localRiskConfig.getIsNoTurnoverWithdraw()) {
            Result<BigDecimal> memberValidBet = gameFeignClient.getMemberValidBet(withdraw.getUid(), null, null, null, lastWithdrawTime, LocalDateTime.now());
            if (memberValidBet.getData().compareTo(ZERO) <= 0) {
                set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.NO_TURNOVER_WITHDRAW));
            }
        }
    }

    private void validRiskForAgent(Set<WithdrawRisk> set, MemberWithdraw withdraw) {
        if (localRiskConfig.getIsFirstWithdraw() &&
                iWithdrawService.count(new LambdaQueryWrapper<MemberWithdraw>().eq(MemberWithdraw::getUid, withdraw.getUid())
                        .eq(MemberWithdraw::getState, APPROVED.getState())
                        .in(MemberWithdraw::getRemitState, SUCCESS.getState(), PAY_SUCCESS.getState())) == 0) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.FIRST_WITHDRAW));
        }
        AffiliateInfoDTO affiliateInfoDTO;
        if (localRiskConfig.getIsFirstWithdrawAfterUpdated() &&
                (affiliateInfoDTO = affiliateFeignClient.getAffiliateInfoById(withdraw.getUid()).getData()) != null &&
                affiliateInfoDTO.getLastInfoModified() != null &&
                iWithdrawService.count(
                        new LambdaQueryWrapper<MemberWithdraw>().eq(MemberWithdraw::getUid, withdraw.getUid())
                                .gt(MemberWithdraw::getApplyTime, affiliateInfoDTO.getLastInfoModified())
                                .lt(MemberWithdraw::getApplyTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(withdraw.getApplyTime()))
                ) == 0) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.FIRST_WITHDRAW_AFTER_UPDATED));
        }
        if (withdraw.getAmount().doubleValue() >= localRiskConfig.getHugeWithdraw()) {
            set.add(new WithdrawRisk(withdraw.getId(), RiskTypeEnum.HUGE_WITHDRAW));
        }
        Date date = new Date();
        LocalDateTime[] todayFloorAndCelling = DateUtil.getTodayFloorAndCelling(date);
        log.info("TodayTimeRange:{},{}", todayFloorAndCelling[0], todayFloorAndCelling[1]);
    }

    private BigDecimal calculateAssociationScore(Long uid, String username, String ip) {
        MemberAssociationRateDTO associationRate = memberFeignClient.userAssociation(username).getData();
        if (associationRate == null) {// 沒有個別會員設定，使用系統設定
            getThreshold();
            associationRate = systemAssociationRate;
        }
        BigDecimal score = ZERO;
        MemberDTO member = memberFeignClient.getMemberById(uid).getData();
        if (member.getRegisterIp() != null && hasAssociation(associationRate.getRegIp(), uid, MemberDTO::getRegisterIp, member.getRegisterIp())) {
            score = score.add(associationRate.getRegIp());
        }
        if (member.getRealName() != null && hasAssociation(associationRate.getRealName(), uid, MemberDTO::getRealName, member.getRealName())) {
            score = score.add(associationRate.getRealName());
        }
        if (member.getLastLoginIp() != null && hasAssociation(associationRate.getLoginIp(), uid, MemberDTO::getLastLoginIp, member.getLastLoginIp())) {
            score = score.add(associationRate.getLoginIp());
        }
        if (member.getDeviceCode() != null && hasAssociation(associationRate.getUuid(), uid, MemberDTO::getDeviceCode, member.getDeviceCode())) {
            score = score.add(associationRate.getUuid());
        }
        if (associationRate.getAccount().compareTo(ZERO) > 0 &&
                memberFeignClient.getMemberSimilarUsername(uid, username) != null) {
            score = score.add(associationRate.getAccount());
        }
        if (associationRate.getWithdrawIp().compareTo(ZERO) > 0 &&
                iWithdrawService.getOne(new LambdaQueryWrapper<MemberWithdraw>().select(MemberWithdraw::getId)
                        .ne(MemberWithdraw::getUid, uid).eq(MemberWithdraw::getApplyIp, ip).last("limit 1")) != null) {
            score = score.add(associationRate.getWithdrawIp());
        }
        if (associationRate.getGameIp().compareTo(ZERO) > 0 &&
                Objects.equals(gameFeignClient.lastMemberGameSession(uid).getData(), ip)) {
            score = score.add(associationRate.getGameIp());
        }
        return score;
    }

    private boolean hasAssociation(BigDecimal rate, Long uid, TypedPropertyGetter<MemberDTO, ?> column, String value) {
        return rate.compareTo(ZERO) > 0 &&
                memberFeignClient.getMemberSameColumn(uid, PropertyUtils.getPropertyName(MemberDTO.class, column), value) != null;
    }

}

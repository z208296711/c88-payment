package com.c88.payment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.admin.api.TagFeignClient;
import com.c88.admin.dto.TagDTO;
import com.c88.common.core.base.IBaseEnum;
import com.c88.common.core.enums.RemitStateEnum;
import com.c88.common.core.enums.RiskTypeEnum;
import com.c88.common.core.enums.WithdrawStateEnum;
import com.c88.common.redis.utils.RedisUtils;
import com.c88.member.api.MemberFeignClient;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.common.core.enums.BalanceChangeTypeLinkEnum;
import com.c88.payment.enums.OrderStatus;
import com.c88.payment.mapper.MemberRemarkMapper;
import com.c88.payment.mapper.WithdrawMapper;
import com.c88.payment.pojo.dto.BaseBehalfPayDTO;
import com.c88.payment.pojo.entity.*;
import com.c88.payment.pojo.form.FindRemitForm;
import com.c88.payment.pojo.form.FindWithdrawForm;
import com.c88.payment.pojo.form.WithdrawForm;
import com.c88.payment.pojo.vo.CheckThirdPartyPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyBehalfPaymentVO;
import com.c88.payment.pojo.vo.withdraw.RemitReportVO;
import com.c88.payment.pojo.vo.withdraw.RemitVO;
import com.c88.payment.pojo.vo.withdraw.WithdrawReportVO;
import com.c88.payment.pojo.vo.withdraw.WithdrawVO;
import com.c88.payment.service.IMerchantPayService;
import com.c88.payment.service.IMerchantService;
import com.c88.payment.service.IMemberWithdrawService;
import com.c88.payment.service.IMerchantPayBankService;
import com.c88.payment.service.thirdparty.IThirdPartPayService;
import com.c88.payment.service.thirdparty.ThirdPartPaymentExecutor;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;
import static com.c88.common.core.constant.TopicConstants.WITHDRAW;
import static com.c88.common.core.enums.RemitStateEnum.*;
import static com.c88.common.core.enums.WithdrawStateEnum.APPROVED;
import static com.c88.payment.constants.Constants.SYSTEM;
import static com.c88.payment.constants.RedisKey.DAILY_WITHDRAW_AMOUNT;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

/**
 * 提款申請管理
 */
@Service
@AllArgsConstructor
public class WithdrawServiceImpl extends ServiceImpl<WithdrawMapper, MemberWithdraw> implements IMemberWithdrawService {

    private final MemberRemarkMapper memberRemarkMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ThirdPartPaymentExecutor thirdPartPaymentExecutor;
    private final IMerchantService iMerchantService;
    private final IMerchantPayService iMerchantPayService;
    private final IMerchantPayBankService merchantPayBankService;
    private final TagFeignClient tagFeignClient;
    private final MemberFeignClient memberFeignClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Gson gson = new Gson();

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<WithdrawVO> queryWithdraw(FindWithdrawForm findWithdrawForm) {
        Page<WithdrawVO> page = baseMapper.queryWithdraw(new Page<>(findWithdrawForm.getPageNum(), findWithdrawForm.getPageSize()), findWithdrawForm);
        List<WithdrawVO> list = page.getRecords();
        if (!list.isEmpty()) {
            List<TagDTO> tags = tagFeignClient.listTags().getData();
            Map<Integer, String> tagMap = tags.parallelStream().collect(Collectors.toMap(TagDTO::getId, TagDTO::getName));

            list.parallelStream().forEach(w -> {
                if (isNotEmpty(w.getRiskTypes())) {
                    List<Double> riskTypes = gson.fromJson(w.getRiskTypes(), List.class);
                    w.setRisks(riskTypes.stream()
                            .map(r -> IBaseEnum.getEnumByValue(r.byteValue(), RiskTypeEnum.class).getDesc()).collect(Collectors.toList()));
                }
                if (isNotEmpty(w.getTagIds())) {
                    w.setTags(Arrays.stream(w.getTagIds()).map(t -> tagMap.get(t)).collect(Collectors.toList()));
                }
            });
        }
        return page;
    }

    @Override
    public List<WithdrawReportVO> listWithdraw(FindWithdrawForm findWithdrawForm) {
        List<WithdrawReportVO> list = baseMapper.listWithdraw(findWithdrawForm);
        if (!list.isEmpty()) {
            List<TagDTO> tags = tagFeignClient.listTags().getData();
            Map<Integer, String> tagMap = tags.parallelStream().collect(Collectors.toMap(TagDTO::getId, TagDTO::getName));

            list.parallelStream().forEach(w -> {
                try {
                    StringBuilder sb = new StringBuilder();
                    if (w.getApplyTime() != null) {
                        sb.append(dateTimeFormatter.format(addTimeZone(w.getApplyTime(), findWithdrawForm.getGmtTime())));
                    } else {
                        sb.append("--");
                    }
                    if (w.getFirstTime() != null) {
                        sb.append("/" + dateTimeFormatter.format(addTimeZone(w.getFirstTime(), findWithdrawForm.getGmtTime())));
                    } else {
                        sb.append("/--");
                    }
                    if (w.getSecondTime() != null) {
                        sb.append("/" + dateTimeFormatter.format(addTimeZone(w.getSecondTime(), findWithdrawForm.getGmtTime())));
                    } else {
                        sb.append("/--");
                    }
                    w.setApplyTimeStr(sb.toString());
                    if (isNotEmpty(w.getRiskTypes())) {
                        List<Double> riskTypes = gson.fromJson(w.getRiskTypes(), List.class);
                        w.setRisks(riskTypes.stream()
                                .map(r -> IBaseEnum.getEnumByValue(r.byteValue(), RiskTypeEnum.class).getDesc()).collect(Collectors.joining(System.lineSeparator())));
                    }
                    if (w.getFirstTime() != null) {
                        w.setFirstSpeed(diffToString(w.getApplyTime(), w.getFirstTime()));
                        if (w.getSecondTime() != null) {
                            w.setSecondSpeed(diffToString(w.getFirstTime(), w.getSecondTime()));
                        } else {
                            w.setSecondSpeed("--");
                        }
                    } else {
                        w.setFirstSpeed("--");
                        w.setSecondSpeed("--");
                    }
                    if (isNotEmpty(w.getTagIds())) {
                        w.setTags(Arrays.stream(w.getTagIds()).map(t -> tagMap.get(t)).collect(Collectors.joining(System.lineSeparator())));
                    }
                    if (w.getState() != null) {
                        w.setStateStr(IBaseEnum.getEnumByValue(w.getState(), WithdrawStateEnum.class).getDesc());
                    }
                    if (w.getFirstUser() == null) {
                        w.setFirstUser("--");
                    }
                    if (w.getSecondUser() == null) {
                        w.setSecondUser("--");
                    }
                    if (w.getRemitUser() == null) {
                        w.setRemitUser("--");
                    }
                    if (w.getAmount() != null) {
                        w.setAmount(w.getAmount().setScale(2));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return list;
    }

    @Override
    public Page<RemitVO> queryRemit(FindRemitForm findRemitForm) {
        Page<RemitVO> page = baseMapper.queryRemit(new Page<>(findRemitForm.getPageNum(), findRemitForm.getPageSize()), findRemitForm);
        List<RemitVO> list = page.getRecords();
        if (!list.isEmpty()) {
            Map<Integer, String> vipMap = memberFeignClient.findMemberVipConfigMap().getData();
            list.parallelStream().forEach(w -> {
                if (w.getVipId() != null) {
                    w.setVip(vipMap.get(w.getVipId()));
                }
                if (w.getSecondUser() != null) {
                    w.setApproveUser(w.getSecondUser());
                    w.setApproveTime(w.getSecondTime());
                } else {
                    w.setApproveUser(w.getFirstUser());
                    w.setApproveTime(w.getFirstTime());
                }
            });
            list.sort(Comparator.comparing(RemitVO::getApplyTime).reversed());
        }
        return page;
    }

    @Override
    public List<RemitReportVO> listRemit(FindRemitForm findRemitForm) {
        List<RemitReportVO> list = baseMapper.listRemit(findRemitForm);
        if (!list.isEmpty()) {
            Map<Integer, String> vipMap = memberFeignClient.findMemberVipConfigMap().getData();
            list.parallelStream().forEach(w -> {
                try {
                    if (w.getApplyTime() != null) {
                        w.setApplyTimeStr(dateTimeFormatter.format(addTimeZone(w.getApplyTime(), findRemitForm.getGmtTime())));
                    }
                    if (w.getFirstTime() != null) {
                        w.setFirstTime(addTimeZone(w.getFirstTime(), findRemitForm.getGmtTime()));
                    }
                    if (w.getSecondTime() != null) {
                        w.setSecondTime(addTimeZone(w.getSecondTime(), findRemitForm.getGmtTime()));
                    }
                    if (w.getRemitTime() != null) {
                        w.setRemitTimeStr(dateTimeFormatter.format(addTimeZone(w.getRemitTime(), findRemitForm.getGmtTime())));
                    } else {
                        w.setRemitTimeStr("--");
                    }
                    if (w.getApplyTime() != null && w.getRemitTime() != null) {
                        w.setSpeed(diffToString(w.getApplyTime(), w.getRemitTime()));
                    } else {
                        w.setSpeed("--");
                    }
                    if (w.getVipId() != null) {
                        w.setVip(vipMap.get(w.getVipId()));
                    }
                    if (w.getSecondUser() != null) {
                        w.setApproveUser("風控二審" + System.lineSeparator() + "（" + w.getSecondUser() + "）");
                        w.setApproveTime(dateTimeFormatter.format(w.getSecondTime()));
                    } else {
                        w.setApproveUser(SYSTEM.equals(w.getFirstUser()) ? "自動審單" : "風控一審" + System.lineSeparator() + "（" + w.getFirstUser() + "）");
                        w.setApproveTime(w.getFirstTime() != null ? dateTimeFormatter.format(w.getFirstTime()) : null);
                    }
                    if (w.getRemitState() != null) {
                        w.setRemitStateStr(IBaseEnum.getEnumByValue(w.getRemitState(), RemitStateEnum.class).getDesc());
                    }
                    if (w.getRemitUser() == null) {
                        w.setRemitUser("--");
                    }
                    if (w.getRemitType() == null) {
                        w.setRemitType("--");
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
        return list;
    }

    private LocalDateTime addTimeZone(LocalDateTime time, Integer timeZone) {
        return time.plusHours(timeZone);
    }

    private String diffToString(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        return (duration.toDaysPart() != 0 ? duration.toDaysPart() + "天" : "") +
                (duration.toHoursPart() != 0 ? duration.toHoursPart() + "時" : "") +
                (duration.toMinutesPart() != 0 ? duration.toMinutesPart() + "分" : "") +
                duration.toSecondsPart() + "秒";
    }

    @Override
    public WithdrawVO getDetail(Long id) {
        MemberWithdraw withdraw = getById(id);

        WithdrawVO withdrawVO = BeanUtil.copyProperties(withdraw, WithdrawVO.class);
        MemberRemark remark = memberRemarkMapper.selectOne(new LambdaQueryWrapper<MemberRemark>()
                .eq(MemberRemark::getUid, withdraw.getUid())
                .orderByDesc(MemberRemark::getGmtCreate)
                .last("limit 1"));
        withdrawVO.setLastRemark(remark != null ? remark.getContent() : null);
        return withdrawVO;
    }

    @Override
    public boolean approve(WithdrawForm withdrawForm, String ip) {
        boolean result = update(new LambdaUpdateWrapper<MemberWithdraw>()
                .set(MemberWithdraw::getState, APPROVED.getState())
                .set(withdrawForm.getLevel() == 1 ? MemberWithdraw::getFirstNote : MemberWithdraw::getSecondNote, withdrawForm.getNote())
                .set(withdrawForm.getLevel() == 1 ? MemberWithdraw::getFirstTime : MemberWithdraw::getSecondTime, LocalDateTime.now())
                .eq(MemberWithdraw::getId, withdrawForm.getId()));
        if (result) {
            com.c88.payment.vo.WithdrawVO withdrawVO = new com.c88.payment.vo.WithdrawVO();
            MemberWithdraw withdraw = getById(withdrawForm.getId());
            withdrawVO.setId(withdraw.getId());
            withdrawVO.setUsername(withdraw.getUsername());
            withdrawVO.setFinishedTime(withdraw.getSecondTime() != null ? withdraw.getSecondTime() :
                    (withdraw.getFirstTime() != null ? withdraw.getFirstTime() : LocalDateTime.now()));
            withdrawVO.setUserIp(ip);
            kafkaTemplate.send(WITHDRAW, withdrawVO.getUsername(), withdrawVO);
        }
        return result;
    }

    @Override
    public boolean executeMerchantPay(MemberWithdraw withdraw, Merchant merchant, String ip) {
        try {
            IThirdPartPayService iThirdPartPayService = thirdPartPaymentExecutor.findByMerchantCode(merchant.getCode());
            BaseBehalfPayDTO baseBehalfPayDTO = new BaseBehalfPayDTO();
            baseBehalfPayDTO.setUid(withdraw.getUid());
            baseBehalfPayDTO.setOrderId(withdraw.getWithdrawNo());
            baseBehalfPayDTO.setAmount(withdraw.getAmount());
            baseBehalfPayDTO.setBankAccount(withdraw.getRealName());
            baseBehalfPayDTO.setBankNo(withdraw.getBankCardNo());
            baseBehalfPayDTO.setBankCode(merchantPayBankService.getOne(new LambdaQueryWrapper<MerchantPayBank>()
                    .eq(MerchantPayBank::getBankId, withdraw.getBankId())
                    .eq(MerchantPayBank::getMerchantPayId, merchant.getId())
                    , false)
                    .getParam());
            baseBehalfPayDTO.setUserIp(ip);
            baseBehalfPayDTO.setMemberId(withdraw.getUid());
            ThirdPartyBehalfPaymentVO thirdPartyBehalfPaymentVO = iThirdPartPayService.createBehalfPayOrder(merchant, baseBehalfPayDTO).getData();
            byte remitState = PAY_PENDING.getState();
            if (thirdPartyBehalfPaymentVO == null) {
                remitState = PAY_FAILED.getState();
            }
            return update(new LambdaUpdateWrapper<MemberWithdraw>()
                    .set(MemberWithdraw::getMerchantId, merchant.getId())
                    .set(MemberWithdraw::getRemitState, remitState)
                    .set(MemberWithdraw::getRemitUser, SYSTEM)
                    .set(MemberWithdraw::getTransactionId, thirdPartyBehalfPaymentVO != null ? thirdPartyBehalfPaymentVO.getTransactionId() : null)
                    .set(MemberWithdraw::getRemitPickTime, LocalDateTime.now())
                    .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                    .eq(MemberWithdraw::getId, withdraw.getId()));
        } catch (Exception e) {
            log.error("fail to execute third party payment:" + e.getMessage());
        }
        return false;
    }

    /**
     * 從代付資料中判斷符合會員等級、標籤，三方餘額、單筆上下限，支援銀行的三方代付
     *
     * @return 合用的三方代付，若沒有則為 null
     */
    @Override
    public Merchant checkSuitableMerchant(MemberWithdraw withdraw) {
        List<MerchantPay> merchantPays = iMerchantPayService.list(new LambdaQueryWrapper<MerchantPay>()
                .eq(MerchantPay::getEnable, 1));
        List<Merchant> merchants = iMerchantService.list(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getEnable, 1));
        List<MerchantPayBank> merchantPayBanks = merchantPayBankService.list();
        Map<Integer, Set<Integer>> merchantPayBankMap = merchantPayBanks.parallelStream().collect(Collectors.toMap(MerchantPayBank::getMerchantPayId,
                b -> {
                    Set<Integer> x = new HashSet<>();
                    x.add(b.getBankId());
                    return x;
                }, (x, y) -> {
                    x.addAll(y);
                    return x;
                }));
        Map<String, Merchant> merchantMap = merchants.parallelStream().collect(Collectors.toMap(Merchant::getCode, Function.identity(), (x, y) -> x));
        List<Integer> memberTagIds = (withdraw.getTagIds()==null || withdraw.getTagIds().length==0)?new ArrayList<>():List.of(withdraw.getTagIds());
        Optional<MerchantPay> optional = merchantPays.parallelStream().filter(p -> {
            try {
                if ((isEmpty(p.getTagIds()) || p.getTagIds().parallelStream().anyMatch(t -> memberTagIds.contains(t))) &&// 只要有一個標籤符合
                        p.getVipIds().parallelStream().anyMatch(v -> Objects.equals(v, withdraw.getVipId()))) {
                    Set<Integer> supportBankIds = merchantPayBankMap.get(p.getId());
                    if (CollectionUtils.isNotEmpty(supportBankIds) && supportBankIds.contains(withdraw.getBankId())) {// 判斷三方代付是否支援會員提款的銀行
                        IThirdPartPayService iThirdPartPayService = thirdPartPaymentExecutor.findByMerchantCode(p.getMerchantCode());
                        if (p.getThresholdBalance().compareTo(iThirdPartPayService.getCompanyBalance(merchantMap.get(p.getMerchantCode()))) < 0) { // 三方的即時餘額需高於此設置的下限水位
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("fail to check merchant pay with merchant code(" + p.getMerchantCode() + "):" + e.getMessage());
            }
            return false;
        }).findFirst();

        return optional.isPresent() ? iMerchantService.getOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getCode, optional.get().getMerchantCode()), false) : null;
    }

    @Override
    public boolean checkPayState(Integer id) {
        MemberWithdraw withdraw = lambdaQuery()
                .eq(MemberWithdraw::getId, id)
                .eq(MemberWithdraw::getRemitState, RemitStateEnum.PAY_PENDING.getState())
                .oneOpt()
                .orElse(MemberWithdraw.builder().build());

        boolean result = true;
        if (withdraw.getMerchantId() != null) {
            Merchant merchant = iMerchantService.getById(withdraw.getMerchantId());
            IThirdPartPayService iThirdPartPayService = thirdPartPaymentExecutor.findByMerchantCode(merchant.getCode());
            CheckThirdPartyPaymentVO checkThirdPartyPaymentVO = iThirdPartPayService.checkWithdrawStatus(merchant, withdraw.getWithdrawNo()).getData();
            if (checkThirdPartyPaymentVO.getStatus() == OrderStatus.SUCCESS.getCode()) {
                result = update(new LambdaUpdateWrapper<MemberWithdraw>()
                        .set(MemberWithdraw::getRemitState, PAY_SUCCESS.getState())
                        .set(MemberWithdraw::getRemitNote, "自動代付成功")
                        .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                        .eq(MemberWithdraw::getId, id));
            } else if (checkThirdPartyPaymentVO.getStatus() == OrderStatus.TRANSACTION_FAIL.getCode() ||
                    checkThirdPartyPaymentVO.getStatus() == OrderStatus.OPERATE_FAIL.getCode() ||
                    checkThirdPartyPaymentVO.getStatus() == OrderStatus.QUERY_FAIL.getCode()) {
                result = update(new LambdaUpdateWrapper<MemberWithdraw>()
                        .set(MemberWithdraw::getRemitState, PAY_FAILED.getState())
                        .set(MemberWithdraw::getRemitNote, "自動代付失敗")
                        .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                        .eq(MemberWithdraw::getId, id));
                if (result) {
                    // 返回會員申請提款時預先扣除的餘額
                    kafkaTemplate.send(BALANCE_CHANGE,
                            AddBalanceDTO.builder()
                                    .memberId(withdraw.getUid())
                                    .balance(withdraw.getAmount())
                                    .balanceChangeTypeLinkEnum(BalanceChangeTypeLinkEnum.WITHDRAW)
                                    .type(BalanceChangeTypeLinkEnum.WITHDRAW.getType())
                                    .betRate(BigDecimal.ZERO)
                                    .note(BalanceChangeTypeLinkEnum.WITHDRAW.getI18n())
                                    .build()
                    );

                    // 返回會員當前等級提款上限金額
                    String key = RedisUtils.buildKey(DAILY_WITHDRAW_AMOUNT, withdraw.getGmtCreate().toLocalDate(), withdraw.getUid());
                    BigDecimal memberDailyAmount = (BigDecimal) redisTemplate.opsForValue().get(key);
                    if (Objects.nonNull(memberDailyAmount)) {
                        redisTemplate.opsForValue().set(key, memberDailyAmount.subtract(withdraw.getAmount()), 3, TimeUnit.DAYS);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public BigDecimal memberTotalWithdraw(Long uid) {
        MemberWithdraw withdraw = getOne(new QueryWrapper<MemberWithdraw>()
                .select("sum(amount)as amount")
                .lambda()
                .eq(MemberWithdraw::getUid, uid)
                .eq(MemberWithdraw::getState, APPROVED.getState())
                .notIn(MemberWithdraw::getRemitState, REVOKED.getState(), CANCEL.getState(),
                        PAY_FAILED.getState(), PAY_FAILED_REAL_NAME.getState())
                .groupBy(MemberWithdraw::getUid));
        return withdraw != null ? withdraw.getAmount() : BigDecimal.ZERO;
    }

}

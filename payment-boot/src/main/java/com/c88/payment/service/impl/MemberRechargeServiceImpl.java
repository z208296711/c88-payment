package com.c88.payment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.common.core.base.BaseEntity;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.enums.RechargeAwardTypeEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.RandomUtils;
import com.c88.common.web.util.UUIDUtils;
import com.c88.common.web.util.UserUtils;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.api.MemberRechargeAwardRecordClient;
import com.c88.member.api.MemberRechargeAwardTemplateClient;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.member.enums.RechargeAwardModeEnum;
import com.c88.member.enums.RechargeAwardRecordModelActionEnum;
import com.c88.member.enums.RechargeAwardRecordStateEnum;
import com.c88.member.event.RechargeAwardRecordModel;
import com.c88.member.vo.AllMemberPersonalRechargeAwardRecordByTemplateIdVO;
import com.c88.member.vo.MemberRechargeAwardTemplateClientVO;
import com.c88.payment.constants.RedisKey;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.dto.MemberRechargeSuccessDTO;
import com.c88.payment.enums.CommentStatusEnum;
import com.c88.payment.enums.MemberRechargeTypeEnum;
import com.c88.payment.enums.PayStatusEnum;
import com.c88.payment.enums.PayTypeEnum;
import com.c88.payment.enums.RechargePayTypeEnum;
import com.c88.payment.enums.RechargeTimeTypeEnum;
import com.c88.payment.enums.RechargeTypeEnum;
import com.c88.payment.form.SearchMemberDepositForm;
import com.c88.payment.mapper.MemberRechargeMapper;
import com.c88.payment.mapstruct.BankConverter;
import com.c88.payment.mapstruct.CompanyBankCardConverter;
import com.c88.payment.mapstruct.MemberChannelConverter;
import com.c88.payment.mapstruct.MemberRechargeConverter;
import com.c88.payment.mapstruct.OnlineMemberRechargeConverter;
import com.c88.payment.mq.sender.MerchantTheFirstAndSecondDelaySender;
import com.c88.payment.pojo.dto.BasePayDTO;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.entity.CompanyBankCard;
import com.c88.payment.pojo.entity.MemberChannel;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.entity.MerchantRechargeChannel;
import com.c88.payment.pojo.entity.MerchantRechargeType;
import com.c88.payment.pojo.entity.RechargeType;
import com.c88.payment.pojo.form.AddOnlineMemberRechargeForm;
import com.c88.payment.pojo.form.ExportOnlineMemberRechargeForm;
import com.c88.payment.pojo.form.FindInlineRechargeForm;
import com.c88.payment.pojo.form.FindOnlineRechargeForm;
import com.c88.payment.pojo.form.InlineMakeUpForm;
import com.c88.payment.pojo.form.MemberRechargeForm;
import com.c88.payment.pojo.form.OnlineMakeUpForm;
import com.c88.payment.pojo.form.RechargeModifyForm;
import com.c88.payment.pojo.vo.AddOnlineMemberRechargeVO;
import com.c88.payment.pojo.vo.ExportOnlineMemberRechargeVO;
import com.c88.payment.pojo.vo.H5MemberChannelVO;
import com.c88.payment.pojo.vo.H5RechargeVO;
import com.c88.payment.pojo.vo.MemberChannelRedis;
import com.c88.payment.pojo.vo.MemberRechargeFormVO;
import com.c88.payment.pojo.vo.MemberRechargeInfo;
import com.c88.payment.pojo.vo.OnlineMemberRechargeVO;
import com.c88.payment.pojo.vo.RechargeInlineExcelVO;
import com.c88.payment.pojo.vo.RechargeTypeVO;
import com.c88.payment.pojo.vo.RechargeVO;
import com.c88.payment.pojo.vo.RechargeWithdrawReportVO;
import com.c88.payment.pojo.vo.ThirdPartyPaymentVO;
import com.c88.payment.pojo.vo.VipConfigSettingLabelVO;
import com.c88.payment.pojo.vo.VipConfigSettingNoteVO;
import com.c88.payment.pojo.vo.VipConfigSettingSortVO;
import com.c88.payment.service.IBankService;
import com.c88.payment.service.ICompanyBankCardService;
import com.c88.payment.service.IMemberChannelService;
import com.c88.payment.service.IMemberChannelVipConfigSettingService;
import com.c88.payment.service.IMemberRechargeService;
import com.c88.payment.service.IMerchantRechargeChannelService;
import com.c88.payment.service.IMerchantRechargeTypeService;
import com.c88.payment.service.IMerchantService;
import com.c88.payment.service.IRechargeTypeService;
import com.c88.payment.service.MemberChannelCommonService;
import com.c88.payment.service.thirdparty.IThirdPartPayService;
import com.c88.payment.service.thirdparty.ThirdPartPaymentExecutor;
import com.c88.payment.vo.MemberDepositDTO;
import com.c88.payment.vo.MemberTotalRechargeVO;
import com.c88.payment.vo.RechargeAwardDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;
import static com.c88.common.core.constant.TopicConstants.RECHARGE_AWARD_RECORD;
import static com.c88.common.core.constant.TopicConstants.RECHARGE_SUCCESS;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.IN_RECHARGE;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.MAKE_IN_RECHARGE;
import static com.c88.common.redis.constant.RedisKey.getRechargeAwardByDayKey;
import static com.c88.common.redis.constant.RedisKey.getRechargeAwardByMonthKey;
import static com.c88.common.redis.constant.RedisKey.getRechargeAwardByTotalKey;
import static com.c88.common.redis.constant.RedisKey.getRechargeAwardByWeekKey;
import static com.c88.payment.constants.CacheConstants.MEMBER_TOTAL_RECHARGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberRechargeServiceImpl extends ServiceImpl<MemberRechargeMapper, MemberRecharge> implements IMemberRechargeService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final ThirdPartPaymentExecutor thirdPartPaymentExecutor;

    private final IMemberChannelService iMemberChannelService;

    private final IRechargeTypeService iRechargeTypeService;

    private final IMerchantRechargeChannelService iMerchantRechargeChannelService;

    private final IBankService iBankService;

    private final ICompanyBankCardService iCompanyBankCardService;

    private final MemberFeignClient memberFeignClient;

    private final IMerchantService iMerchantService;

    private final OnlineMemberRechargeConverter onlineMemberRechargeConverter;

    private final MemberChannelConverter memberChannelConverter;

    private final BankConverter bankConverter;

    private final CompanyBankCardConverter companyBankCardConverter;

    private final MerchantTheFirstAndSecondDelaySender merchantTheFirstAndSecondDelaySender;

    private final IMemberChannelVipConfigSettingService iMemberChannelVipConfigSettingService;

    private final MemberChannelCommonService memberChannelCommon;

    private final IMerchantRechargeTypeService iMerchantRechargeTypeService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final MemberRechargeConverter memberRechargeConverter;

    private final MemberRechargeAwardTemplateClient memberRechargeAwardTemplateClient;

    private final MemberRechargeAwardRecordClient memberRechargeAwardRecordClient;

    private final MemberRechargeMapper rechargeMapper;

    @Override
    public IPage<OnlineMemberRechargeVO> findOnlineRecharge(FindOnlineRechargeForm form) {
        RechargeTimeTypeEnum timeTypeEnum = RechargeTimeTypeEnum.getEnum(form.getTimeType());
        return this.lambdaQuery()
                .gt(Objects.equals(RechargeTimeTypeEnum.RECEIVE, timeTypeEnum) && form.getStartTime() != null, MemberRecharge::getGmtCreate, form.getStartTime())
                .lt(Objects.equals(RechargeTimeTypeEnum.RECEIVE, timeTypeEnum) && form.getEndTime() != null, MemberRecharge::getGmtCreate, form.getEndTime())
                .gt(Objects.equals(RechargeTimeTypeEnum.TRANSACTION, timeTypeEnum) && form.getStartTime() != null, MemberRecharge::getSuccessTime, form.getStartTime())
                .lt(Objects.equals(RechargeTimeTypeEnum.TRANSACTION, timeTypeEnum) && form.getEndTime() != null, MemberRecharge::getSuccessTime, form.getEndTime())
                .eq(Objects.nonNull(form.getTradeNo()), MemberRecharge::getTradeNo, form.getTradeNo())
                .eq(StringUtils.isNotBlank(form.getMemberLevelName()), MemberRecharge::getMemberLevelName, form.getMemberLevelName())
                .eq(Objects.nonNull(form.getStatus()), MemberRecharge::getStatus, form.getStatus())
                .eq(Objects.nonNull(form.getUsername()), MemberRecharge::getUsername, form.getUsername())
                .eq(Objects.nonNull(form.getMerchantId()), MemberRecharge::getMerchantId, form.getMerchantId())
                .eq(Objects.nonNull(form.getRechargeTypeId()), MemberRecharge::getRechargeTypeId, form.getRechargeTypeId())
                .in(MemberRecharge::getType, MemberRechargeTypeEnum.ONLINE.getValue(), MemberRechargeTypeEnum.ONLINE_MAKE_UP.getValue())
                .orderByDesc(MemberRecharge::getId)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(onlineMemberRechargeConverter::toVo);
    }

    @Override
    public IPage<RechargeVO> findInlineRecharge(FindInlineRechargeForm form) {
        return baseMapper.queryInlineRechargeList(new Page<>(form.getPageNum(), form.getPageSize()), form);
    }

    //取得輪替的支付通道
    public MemberChannel findCurrentMemberChannel(MemberInfoDTO memberInfoDTO, Integer rechargeId) {
        MemberChannel memberChannel = (MemberChannel) redisTemplate.opsForList().leftPop(RedisKey.getChooseChannelQueueKey(memberInfoDTO.getCurrentVipId(), rechargeId));
        if (memberChannel == null) {
            List<MemberChannelRedis> memberChannelRedisList = memberChannelCommon.findMemberChannel(memberInfoDTO.getCurrentVipId(), rechargeId);
            if (CollectionUtils.isEmpty(memberChannelRedisList)) {
                return null;
            }
            List<MemberChannel> memberChannelList = memberChannelRedisList.stream().map(x -> {
                MemberChannel channel = new MemberChannel();
                BeanUtil.copyProperties(x, channel);
                channel.setVipIds(Arrays.stream(x.getVipIds().get(0).split(",")).map(Long::valueOf).collect(Collectors.toList()));
                if (CollectionUtils.isNotEmpty(x.getMemberTags())) {
                    channel.setMemberTags(Arrays.stream(x.getMemberTags().get(0).split(",")).map(Long::valueOf).collect(Collectors.toList()));
                }
                return channel;
            }).collect(Collectors.toList());

            // 新增至redis
            memberChannelList.forEach(channel -> redisTemplate.opsForList().rightPush(RedisKey.getChooseChannelQueueKey(memberInfoDTO.getCurrentVipId(), rechargeId), channel));

            memberChannel = (MemberChannel) redisTemplate.opsForList().leftPop(RedisKey.getChooseChannelQueueKey(memberInfoDTO.getCurrentVipId(), rechargeId));
        }
        redisTemplate.opsForList().rightPush(RedisKey.getChooseChannelQueueKey(memberInfoDTO.getCurrentVipId(), rechargeId), memberChannel);

        return getQueueChannel(memberInfoDTO, memberChannel, rechargeId);
    }

    public MemberChannel getQueueChannel(MemberInfoDTO memberInfoDTO, MemberChannel memberChannel, Integer rechargeId) {
        Long size = redisTemplate.opsForList().size(RedisKey.getChooseChannelQueueKey(memberInfoDTO.getCurrentVipId(), rechargeId));
        long count = 0L;
        List<Long> longs = memberChannel.getMemberTags();
        while (CollectionUtils.isNotEmpty(memberChannel.getMemberTags()) &&
                memberChannel.getMemberTags().stream().noneMatch(x -> memberInfoDTO.getTagIdList().contains(x.intValue()))) {
            if (count >= size || size == 1) {
                return memberChannel;
            }
            memberChannel = (MemberChannel) redisTemplate.opsForList().leftPop(RedisKey.getChooseChannelQueueKey(memberInfoDTO.getCurrentVipId(), rechargeId));
            redisTemplate.opsForList().rightPush(RedisKey.getChooseChannelQueueKey(memberInfoDTO.getCurrentVipId(), rechargeId), memberChannel);
            count++;
        }
        return memberChannel;
    }

    public H5RechargeVO findMemberRecharges(Long memberId) {
        Result<MemberInfoDTO> memberInfoRes = memberFeignClient.getMemberInfo(memberId);
        if (!Result.isSuccess(memberInfoRes)) {
            return null;
        }
        MemberInfoDTO memberInfoDTO = memberInfoRes.getData();

        List<RechargeTypeVO> rechargeTypeVOList = this.findMemberRecharge(memberInfoDTO);
        List<VipConfigSettingNoteVO> noteList = iMemberChannelVipConfigSettingService.getVipConfigSettingNote(memberInfoDTO.getCurrentVipId());
        List<String> strings = noteList.stream().map(VipConfigSettingNoteVO::getNote).filter(note -> !note.isEmpty()).collect(Collectors.toList());
        H5RechargeVO vo = new H5RechargeVO();
        vo.setRechargeTypes(rechargeTypeVOList);
        vo.setNoteList(strings);
        return vo;
    }

    @Override
    public List<MemberTotalRechargeVO> findMemberTotalRecharge(LocalDateTime startTime, LocalDateTime endTime) {
        return this.baseMapper.findMemberTotalRecharge(startTime, endTime);
    }

    @Override
    @Transactional
    public List<RechargeTypeVO> findMemberRecharge(MemberInfoDTO memberInfoDTO) {

        Map<String, RechargeType> rechargeTypesMap = iRechargeTypeService.findRechargeTypeMap();

        List<MemberChannel> memberChannelList = memberChannelCommon.findUsableMemberChannel(memberInfoDTO, null);

        List<MemberChannel> memberChannelByCompanyList = memberChannelCommon.findUsableMemberChannelByCompany(memberInfoDTO, RechargeTypeEnum.COMPANY_BANK_CARD.getValue());
        memberChannelList.addAll(memberChannelByCompanyList);

        Map<Integer, List<MemberChannel>> rechargeTypeVOSMap = memberChannelList
                .stream()
                .collect(Collectors.groupingBy(MemberChannel::getRechargeTypeId));
        List<VipConfigSettingSortVO> sortList = iMemberChannelVipConfigSettingService.getVipConfigSettingSort(memberInfoDTO.getCurrentVipId());

        List<Integer> rechargeTypeIds = sortList.stream()
                .filter(x -> rechargeTypeVOSMap.containsKey(x.getRechargeTypeId()))
                .sorted(Comparator.comparing(VipConfigSettingSortVO::getSort))
                .map(VipConfigSettingSortVO::getRechargeTypeId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(rechargeTypeIds)) {
            return new ArrayList<>();
        }

        List<VipConfigSettingLabelVO> list = iMemberChannelVipConfigSettingService.getVipConfigSettingLabel(memberInfoDTO.getCurrentVipId());

        Map<Integer, VipConfigSettingLabelVO> vipConfigSettingLabelVOMap = list.stream()
                .collect(Collectors.toMap(VipConfigSettingLabelVO::getRechargeTypeId, Function.identity()));

        // 取得支付方式設定
        List<MerchantRechargeType> merchantRechargeTypes = iMerchantRechargeTypeService.lambdaQuery()
                .eq(MerchantRechargeType::getEnable, EnableEnum.START.getCode())
                .list();

        // 取得銀行
        List<Bank> banks = iBankService.lambdaQuery()
                .in(Bank::getState, List.of(1, 2))
                .list();

        Map<Integer, List<MerchantRechargeChannel>> merchantRechargeChannelMap = iMerchantRechargeChannelService
                .lambdaQuery()
                .in(MerchantRechargeChannel::getRechargeTypeId, rechargeTypeIds)
                .eq(MerchantRechargeChannel::getEnable, EnableEnum.START.getCode())
                .list()
                .stream()
                .collect(Collectors.groupingBy(MerchantRechargeChannel::getRechargeTypeId));

        return rechargeTypeIds
                .stream()
                .map(rechargeId -> {
                    MemberChannel memberChannel;
                    String key = RedisKey.getChooseMemberChannelStrategyKey(memberInfoDTO.getCurrentVipId(), rechargeId);
                    Integer random = (Integer) redisTemplate.opsForValue().get(key);
                    if (random != null && random == 1) {
                        memberChannel = selectMatchMemberChannel(memberChannelList, rechargeId);
                    } else {
                        memberChannel = findCurrentMemberChannel(memberInfoDTO, rechargeId);
                    }

                    if (memberChannel == null) {
                        return null;
                    }
                    Optional<MerchantRechargeType> merchantRechargeTypeOpt = merchantRechargeTypes.stream()
                            .filter(filter -> filter.getMerchantId().intValue() == memberChannel.getMerchantId() && filter.getRechargeTypeId().intValue() == memberChannel.getRechargeTypeId())
                            .findFirst();
                    if (merchantRechargeTypeOpt.isEmpty() && rechargeId != 7) {
                        return null;
                    }

                    H5MemberChannelVO h5MemberChannelVO = memberChannelConverter.toVO(memberChannel);
                    h5MemberChannelVO.setFeeRate(merchantRechargeTypeOpt.map(MerchantRechargeType::getFeeRate).orElse(BigDecimal.ZERO));
                    List<MerchantRechargeChannel> merchantRechargeChannelList = merchantRechargeChannelMap.getOrDefault(memberChannel.getRechargeTypeId(), Collections.emptyList());
                    if (CollectionUtils.isEmpty(merchantRechargeChannelList)) {
                        h5MemberChannelVO = null;
                    }
                    RechargeType rechargeType = rechargeTypesMap.get(String.valueOf(rechargeId));
                    RechargeTypeVO rechargeTypeVO = new RechargeTypeVO();
                    rechargeTypeVO.setId(rechargeId);
                    rechargeTypeVO.setName(rechargeType.getName());
                    rechargeTypeVO.setSort(rechargeType.getSort());
                    rechargeTypeVO.setLabel(vipConfigSettingLabelVOMap.getOrDefault(rechargeId, new VipConfigSettingLabelVO()).getLabel());
                    rechargeTypeVO.setChannel(h5MemberChannelVO);
                    rechargeTypeVO.setType(rechargeType.getType());
                    // 跳轉到3方類型的不需要銀行
                    if (!RechargePayTypeEnum.NORMAL.getValue().equals(rechargeType.getType())) {
                        rechargeTypeVO.setRechargeInfo(findMemberRechargeInfo(memberChannel, banks));
                    }
                    return rechargeTypeVO;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private MemberChannel selectMatchMemberChannel(List<MemberChannel> memberChannels, Integer rechargeTypeId) {
        Iterator<MemberChannel> channelIterator = memberChannels.iterator();
        //因為在塞進Redis時，沒有過濾已關閉的，所以在這邊重新篩一次
        while (channelIterator.hasNext()) {
            MemberChannel memberChannel = channelIterator.next();
            //廠商是否關閉
            if (iMerchantService.getMerchantById(memberChannel.getMerchantId().longValue()).getEnable() == 0) {
                channelIterator.remove();
                continue;
            }
            //type
            if (iMerchantRechargeTypeService.lambdaQuery()
                    .eq(MerchantRechargeType::getMerchantId, memberChannel.getMerchantId())
                    .eq(MerchantRechargeType::getRechargeTypeId, rechargeTypeId)
                    .one()
                    .getEnable() == 0) {
                channelIterator.remove();
                continue;
            }
            //通道
            if (memberChannel.getStatus() == 0) {
                channelIterator.remove();
            }
        }

        return RandomUtils.getRandomElement(memberChannels.stream().filter(filter -> Objects.equals(filter.getRechargeTypeId(), rechargeTypeId)).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public AddOnlineMemberRechargeVO addOnlineMemberRecharge(Long memberId, AddOnlineMemberRechargeForm form, HttpServletRequest request) {
        Result<MemberInfoDTO> memberResult = memberFeignClient.getMemberInfo(memberId);
        if (!Result.isSuccess(memberResult)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }

        MemberInfoDTO member = memberResult.getData();

        //取得對應的會員通道，檢查最大與最小金額
        MemberChannel memberChannel = iMemberChannelService.lambdaQuery()
                .eq(MemberChannel::getId, form.getMemberChannelId())
                .eq(MemberChannel::getType, PayTypeEnum.THIRD_PARTY.getValue())
                .eq(MemberChannel::getStatus, EnableEnum.START.getCode())
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));
        if (form.getAmount().compareTo(memberChannel.getMaxRechargeAmount()) > 0 || form.getAmount().compareTo(memberChannel.getMinRechargeAmount()) < 0) {
            throw new BizException(ResultCode.RECHARGE_AMOUNT_INVALID);
        }

        // 取得通道
        MerchantRechargeChannel merchantRechargeChannel = iMerchantRechargeChannelService.lambdaQuery()
                .eq(MerchantRechargeChannel::getMerchantId, memberChannel.getMerchantId())
                .eq(MerchantRechargeChannel::getRechargeTypeId, memberChannel.getRechargeTypeId())
                .eq(MerchantRechargeChannel::getEnable, EnableEnum.START.getCode())
                .eq(Objects.nonNull(form.getBankId()), MerchantRechargeChannel::getBankId, form.getBankId())
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        // 取得商戶
        Merchant merchant = iMerchantService.lambdaQuery()
                .eq(Merchant::getId, memberChannel.getMerchantId())
                .eq(Merchant::getEnable, EnableEnum.START.getCode())
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        // 取得支付方式設定
        MerchantRechargeType merchantRechargeType = iMerchantRechargeTypeService.lambdaQuery()
                .eq(MerchantRechargeType::getMerchantId, memberChannel.getMerchantId())
                .eq(MerchantRechargeType::getRechargeTypeId, memberChannel.getRechargeTypeId())
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        // 取得存送優惠資訊
        Optional<MemberRechargeAwardTemplateClientVO> memberRechargeAwardTemplateOpt = Optional.ofNullable(this.getMemberRechargeAwardTemplateClientVO(form.getRechargeAwardId(), form.getAmount()));

        // 檢查存送優惠充值與次數
        checkRechargeAward(memberRechargeAwardTemplateOpt, memberId);

        // 取得銀行
        Bank bank = Optional.ofNullable(iBankService.getById(form.getBankId())).orElse(Bank.builder().build());

        BigDecimal feeAmount = form.getAmount().multiply(merchantRechargeType.getFeeRate().divide(new BigDecimal("100"), 5, RoundingMode.CEILING));

        String orderId = UUIDUtils.genOnlineOrderId();
        MemberRecharge memberRecharge = MemberRecharge.builder()
                .tradeNo(orderId)
                .merchantId(memberChannel.getMerchantId().longValue())
                .rechargeTypeId(merchantRechargeChannel.getRechargeTypeId())
                .username(member.getUsername())
                .memberId(memberId)
                .memberLevelName(member.getCurrentVipName())
                .realName(member.getRealName())
                .bank(bank.getRechargeName())
                .amount(form.getAmount())
                .fee(feeAmount)
                .rechargeAwardId(form.getRechargeAwardId())
                .rechargeAwardName(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getName).orElse(null))
                .rechargeAwardBetRate(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getBetRate).orElse(null))
                .rechargeAwardAmount(memberRechargeAwardTemplateOpt.map(template -> rechargeAwardAmount(template, form.getAmount())).orElse(null))
                .maxAwardAmount(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getMaxAwardAmount).orElse(null))
                .type(MemberRechargeTypeEnum.ONLINE.getValue())
                .status(PayStatusEnum.IN_PROGRESS.getValue())
                .memberChannelId(memberChannel.getId())
                .memberChannelName(memberChannel.getChannelName())
                .actionIp(ServletUtil.getClientIP(request))
                .build();

        this.save(memberRecharge);

        IThirdPartPayService thirdPartPayService = thirdPartPaymentExecutor.findByMerchantCode(memberChannel.getMerchantCode());
        BasePayDTO basePayDTO = new BasePayDTO();
        basePayDTO.setOrderId(orderId);
        basePayDTO.setChannelId(merchantRechargeChannel.getParam());
        basePayDTO.setAmount(form.getAmount());
        basePayDTO.setRechargeBankCode(merchantRechargeChannel.getRechargeBankCode());
        basePayDTO.setUserIp(ServletUtil.getClientIP(request));
        basePayDTO.setMemberId(memberId);
        basePayDTO.setMemberUsername(member.getUsername());
        basePayDTO.setMemberPhone(member.getMobile());

        Result<ThirdPartyPaymentVO> paymentVOResult = thirdPartPayService.createPayOrder(merchant, basePayDTO);
        if (!Result.isSuccess(paymentVOResult)) {
            memberRecharge.setStatus(PayStatusEnum.FAIL.getValue());
            this.save(memberRecharge);
            throw new BizException(paymentVOResult.getMsg());
        }

        // 建立訂單成功，發送消息檢查訂單狀態
        merchantTheFirstAndSecondDelaySender.sendMessage(memberRecharge.getId());

//        // 建立提款限額紀錄
//        iMemberWithdrawLimitRecordService.save(
//                MemberWithdrawLimitRecord.builder()
//                        .memberId(member.getId())
//                        .username(member.getUsername())
//                        .type(WithdrawLimitTypeEnum.RECHARGE.getValue())
//                        .withdrawLimit(form.getAmount())
//                        .beforeTotalWithdrawLimit(member.getWithdrawLimit())
//                        .afterTotalWithdrawLimit(form.getAmount().add(member.getWithdrawLimit()))
//                        .build()
//        );

        return AddOnlineMemberRechargeVO.builder()
                .payURL(paymentVOResult.getData().getPayUrl())
                .amount(paymentVOResult.getData().getAmount())
                .build();
    }

    @Override
    @Transactional
    public MemberRechargeFormVO inlineRecharge(Long memberId, MemberRechargeForm form, HttpServletRequest request) {
        CompanyBankCard bankCard = iCompanyBankCardService.getById(form.getCompanyCardId());
        checkBankCardDailyMaxAmount(form.getAmount(), bankCard);

        Optional<MemberInfoDTO> memberOpt = Optional.empty();
        Result<MemberInfoDTO> memberById = memberFeignClient.getMemberInfo(memberId);
        if (Result.isSuccess(memberById)) {
            memberOpt = Optional.ofNullable(memberById.getData());
        }
        MemberInfoDTO member = memberOpt.orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        // 取得存送優惠資訊
        Optional<MemberRechargeAwardTemplateClientVO> memberRechargeAwardTemplateOpt = Optional.ofNullable(this.getMemberRechargeAwardTemplateClientVO(form.getRechargeAwardId(), form.getAmount()));

        // 檢查存送優惠充值與次數
        checkRechargeAward(memberRechargeAwardTemplateOpt, memberId);

        Bank bank = Optional.ofNullable(iBankService.getById(bankCard.getBankId())).orElse(Bank.builder().build());
        String comment = null;
        if (bankCard.getComments().equals(CommentStatusEnum.ENABLE.getValue())) {
            comment = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        }

        MemberRecharge memberRecharge = MemberRecharge.builder()
                .tradeNo(UUIDUtils.genInlineOrderId())
                .username(member.getUsername())
                .memberId(memberId)
                .realName(member.getRealName())
                .memberLevelName(member.getCurrentVipName())
                .amount(form.getAmount())
                .rechargeTypeId(form.getRechargeTypeId())
                .rechargeAwardId(form.getRechargeAwardId())
                .rechargeAwardName(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getName).orElse(null))
                .rechargeAwardBetRate(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getBetRate).orElse(null))
                .rechargeAwardAmount(memberRechargeAwardTemplateOpt.map(template -> rechargeAwardAmount(template, form.getAmount())).orElse(null))
                .maxAwardAmount(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getMaxAwardAmount).orElse(null))
                .bankCardCode(bankCard.getCode())
                .bank(bank.getRechargeName())
                .rechargeTypeId(form.getRechargeTypeId())
                .type(MemberRechargeTypeEnum.INLINE.getValue())
                .status(PayStatusEnum.IN_PROGRESS.getValue())
                .notes(comment)
                .actionIp(ServletUtil.getClientIP(request))
                .build();
        this.save(memberRecharge);

        return MemberRechargeFormVO.builder()
                .amount(memberRecharge.getAmount())
                .bank(memberRecharge.getBank())
                .owner(bankCard.getOwner())
                .bankCardNo(bankCard.getBankCardNo())
                .comment(comment)
                .notes(bankCard.getNote())
                .build();
    }

    @Override
    @CacheEvict(cacheNames = MEMBER_TOTAL_RECHARGE, key = "#form.uid", condition = "#form.uid!=null")
    public boolean updateRecharge(RechargeModifyForm form) {
        if (form.getStatus().equals(PayStatusEnum.SUCCESS.getValue())) {
            MemberRecharge recharge = getById(form.getId());
            form.setUid(recharge.getMemberId());// 設定 uid 用以清除cache
            form.setTradeNo(recharge.getTradeNo());
            return checkInlineRecharge(form.getId(), form.getRemark());
        } else {
            LocalDate localDate = LocalDate.now(ZoneId.of("+7"));
            String date = localDate.toString();
            MemberRecharge recharge = getById(form.getId());
            form.setTradeNo(recharge.getTradeNo());
            BigDecimal currentAmount;
            BigDecimal amount = recharge.getAmount();

            if (recharge.getRechargeTypeId().equals(RechargeTypeEnum.COMPANY_BANK_CARD.getValue()) &&
                    recharge.getGmtCreate().atZone(ZoneId.of("+7")).toLocalDate().compareTo(localDate) >= 0) {
                CompanyBankCard bankCard = iCompanyBankCardService.getOne(new LambdaQueryWrapper<CompanyBankCard>()
                        .eq(CompanyBankCard::getCode, recharge.getBankCardCode()));
                Optional<Object> redisObj = Optional.ofNullable(redisTemplate.opsForValue()
                        .get(RedisKey.CARD_DAILY_TOTAL_AMOUNT + ":" + bankCard.getCode() + ":" + date));
                currentAmount = new BigDecimal(redisObj.orElseThrow(
                        () -> new BizException(ResultCode.SYSTEM_EXECUTION_ERROR)).toString());
                redisTemplate.opsForValue().set(RedisKey.CARD_DAILY_TOTAL_AMOUNT + ":" + bankCard.getCode() + ":" + date,
                        currentAmount.subtract(amount).toString());
            }

            MemberRecharge memberRecharge;
            memberRecharge = BeanUtil.copyProperties(form, MemberRecharge.class);
            memberRecharge.setCheckUser(UserUtils.getUsername());

            // 更新狀態成功判斷有使用存送優惠時增加使用次數
            boolean update = this.updateById(memberRecharge);
            if (update) {
                // 增加優惠使用次數
                redisTemplate.opsForValue().increment(getRechargeAwardByDayKey(localDate, recharge.getRechargeAwardId(), recharge.getMemberId()), -1);
                redisTemplate.opsForValue().increment(getRechargeAwardByWeekKey(recharge.getRechargeAwardId(), recharge.getMemberId()), -1);
                redisTemplate.opsForValue().increment(getRechargeAwardByMonthKey(localDate, recharge.getRechargeAwardId(), recharge.getMemberId()), -1);
                redisTemplate.opsForValue().increment(getRechargeAwardByTotalKey(recharge.getRechargeAwardId(), recharge.getMemberId()), -1);
            }

            return update;
        }
    }

    @Override
    @Cacheable(cacheNames = MEMBER_TOTAL_RECHARGE, key = "#uid", condition = "#fromTime==null")
    public BigDecimal memberTotalRecharge(Long uid, LocalDateTime fromTime) {
        MemberRecharge memberRecharge = baseMapper.selectOne(new QueryWrapper<MemberRecharge>()
                .select("sum(amount) as amount")
                .lambda()
                .eq(MemberRecharge::getMemberId, uid)
                .eq(MemberRecharge::getStatus, PayStatusEnum.SUCCESS.getValue())
                .gt(fromTime != null, MemberRecharge::getSuccessTime, fromTime)
                .groupBy(MemberRecharge::getMemberId));
        return memberRecharge != null ? memberRecharge.getAmount() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal memberTotalRecharge(Long uid, LocalDateTime fromTime, LocalDateTime toTime) {
        MemberRecharge memberRecharge = baseMapper.selectOne(new QueryWrapper<MemberRecharge>()
                .select("sum(amount) as amount")
                .lambda()
                .eq(MemberRecharge::getMemberId, uid)
                .eq(MemberRecharge::getStatus, PayStatusEnum.SUCCESS.getValue())
                .gt(fromTime != null, MemberRecharge::getSuccessTime, fromTime)
                .lt(toTime != null, MemberRecharge::getSuccessTime, toTime)
                .groupBy(MemberRecharge::getMemberId));
        return memberRecharge != null ? memberRecharge.getAmount() : BigDecimal.ZERO;
    }

    @Override
    public List<RechargeInlineExcelVO> findInlineRechargeForExcel(FindInlineRechargeForm form) {
        List<RechargeInlineExcelVO> rechargeInlineExcelVOS = this.baseMapper.queryInlineRechargeListFormExcel(form);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        rechargeInlineExcelVOS.forEach(vo -> {
                    vo.setGmtCreate(vo.getGmtCreate() != null ? LocalDateTime.parse(vo.getGmtCreate(), dtf).plusHours(form.getGmtTime()).format(dtf) : "--");
                    vo.setSuccessTime(vo.getSuccessTime() != null ? LocalDateTime.parse(vo.getSuccessTime(), dtf).plusHours(form.getGmtTime()).format(dtf) : "--");
                }
        );
        return rechargeInlineExcelVOS;
    }

    @Override
    public MemberRechargeInfo findMemberRechargeInfo(MemberChannel channel, List<Bank> banks) {
        MemberRechargeInfo rechargeInfo = new MemberRechargeInfo();
        if (channel.getRechargeTypeId() == RechargeTypeEnum.COMPANY_BANK_CARD.getValue()) {
            Long groupId = channel.getCompanyBankCardGroupId();
            // 選自營卡群組
            List<CompanyBankCard> cardList = iCompanyBankCardService.findCompanyCardCanUsedByGroupId(Collections.singletonList(groupId));
            // 選卡片
            CompanyBankCard card = this.randomGetCard(cardList);
            if (Objects.nonNull(card)) {
                banks.stream().filter(filter -> filter.getId().intValue() == card.getBankId()).findFirst().ifPresent(bank -> {
                    rechargeInfo.setCompanyBankCard(companyBankCardConverter.toH5CompanyBankCardVO(card));
                    rechargeInfo.setBanks(Collections.singletonList(bankConverter.toH5BankVO(bank)));
                });
            } else {
                rechargeInfo.setBanks(Collections.emptyList());
            }
        } else {
            // 3方商戶銀行
            List<Bank> bank = iMerchantRechargeChannelService.findMatchRechargeChannelBanks(channel);
            rechargeInfo.setBanks(bankConverter.toH5BankVO(bank));
        }

        return rechargeInfo;
    }

    @Transactional
    @Override
    public Boolean checkInlineRecharge(Long rechargeId, String remark) {
        MemberRecharge memberRecharge = Optional.ofNullable(this.getById(rechargeId)).orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));
        if (!Objects.equals(memberRecharge.getStatus(), PayStatusEnum.IN_PROGRESS.getValue())) {
            throw new BizException(ResultCode.ORDER_COMPLETED);
        }

        // 取得存送優惠資訊
        Optional<MemberRechargeAwardTemplateClientVO> memberRechargeAwardTemplateOpt = Optional.ofNullable(this.getMemberRechargeAwardTemplateClientVO(memberRecharge.getRechargeAwardId(), memberRecharge.getAmount()));

        // 加值金額
        kafkaTemplate.send(BALANCE_CHANGE,
                AddBalanceDTO.builder()
                        .memberId(memberRecharge.getMemberId())
                        .orderId(memberRecharge.getTradeNo())
                        .balance(memberRecharge.getAmount())
                        .balanceChangeTypeLinkEnum(IN_RECHARGE)
                        .type(IN_RECHARGE.getType())
                        .betRate(BigDecimal.ONE)
                        .note(IN_RECHARGE.getI18n())
                        .rechargeAwardDTO(
                                memberRechargeAwardTemplateOpt.map(template ->
                                                RechargeAwardDTO.builder()
                                                        .templateId(template.getId())
                                                        .name(template.getName())
                                                        .type(template.getType())
                                                        .mode(template.getMode())
                                                        .betRate(template.getBetRate())
                                                        .rate(template.getRate())
                                                        .fixAmount(template.getAmount())
                                                        .minJoinAmount(template.getMinJoinAmount())
                                                        .maxAwardAmount(template.getMaxAwardAmount())
                                                        .build()
                                        )
                                        .orElse(null)
                        )
                        .build()
        );

        // 金額加值成功更新訂單狀態
        memberRecharge.setRealAmount(memberRecharge.getAmount().add(memberRecharge.getRechargeAwardAmount()));
        memberRecharge.setStatus(PayStatusEnum.SUCCESS.getValue());
        memberRecharge.setRemark(remark);
        memberRecharge.setSuccessTime(LocalDateTime.now());
        memberRecharge.setCheckUser(UserUtils.getUsername());
        Boolean result = this.updateById(memberRecharge);
        if (!result) {
            return false;
        }

        if (memberRechargeAwardTemplateOpt.isPresent()) {
            MemberRechargeAwardTemplateClientVO memberRechargeAwardTemplate = memberRechargeAwardTemplateOpt.get();
            kafkaTemplate.send(RECHARGE_AWARD_RECORD,
                    RechargeAwardRecordModel.builder()
                            .action(Objects.equals(memberRechargeAwardTemplate.getType(), RechargeAwardTypeEnum.PLATFORM.getCode()) ?
                                    RechargeAwardRecordModelActionEnum.ADD.getCode() :
                                    RechargeAwardRecordModelActionEnum.MODIFY.getCode()
                            )
                            .memberId(memberRecharge.getMemberId())
                            .username(memberRecharge.getUsername())
                            .templateId(memberRechargeAwardTemplate.getId())
                            .name(memberRechargeAwardTemplate.getName())
                            .type(memberRechargeAwardTemplate.getType())
                            .mode(memberRechargeAwardTemplate.getMode())
                            .rate(memberRechargeAwardTemplate.getRate())
                            .betRate(memberRechargeAwardTemplate.getBetRate())
                            .amount(memberRechargeAwardTemplate.getAmount())
                            .rechargeAmount(memberRecharge.getAmount())
                            .useTime(LocalDateTime.now())
                            .state(RechargeAwardRecordStateEnum.USED.getCode())
                            .build()
            );
        }

        // 新增存款成功事件
        MemberRechargeSuccessDTO memberRechargeSuccessDTO = memberRechargeConverter.toMemberRechargeSuccessDTO(memberRecharge);
        RechargeType rechargeTypeById = iRechargeTypeService.getById(memberRechargeSuccessDTO.getRechargeTypeId());
        memberRechargeSuccessDTO.setRechargeTypeI18nKey(rechargeTypeById.getName());
        kafkaTemplate.send(RECHARGE_SUCCESS, memberRechargeSuccessDTO);

        return true;
    }

    public static void main(String[] args) {

    }

    public int searchInsert(int[] nums, int target) {
        int start = 0;
        int end = nums.length - 1;

        while (end >= start) {
            int middle = (end + start) >>> 1;
            if (target == nums[middle]) {
                return middle;
            } else if (target > nums[middle]) {
                start = middle + 1;
            } else {
                end = middle - 1;
            }
        }
        return start;
    }

    private void checkRechargeAwardAmount(Long memberId, MemberRechargeAwardTemplateClientVO template) {
        LocalDate nowDate = LocalDate.now();

        // 判斷昨日充值
        BigDecimal yesterdayRechargeAmount = template.getYesterdayRechargeAmount();
        if (yesterdayRechargeAmount.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal amount = this.memberTotalRecharge(memberId,
                    LocalDateTime.of(nowDate.minusDays(1), LocalTime.MIN),
                    LocalDateTime.of(nowDate.minusDays(1), LocalTime.MAX));
            if (yesterdayRechargeAmount.compareTo(amount) > 0) {
                throw new BizException(ResultCode.YESTERDAY_RECHARGE_AMOUNT_INSUFFICIENT);
            }
        }

        // 判斷上週充值
        BigDecimal lastWeekRechargeAmount = template.getLastWeekRechargeAmount();
        if (lastWeekRechargeAmount.compareTo(BigDecimal.ZERO) != 0) {
            int week = nowDate.getDayOfWeek().getValue();
            BigDecimal amount = this.memberTotalRecharge(memberId,
                    LocalDateTime.of(nowDate.minusDays(week).minusDays(6), LocalTime.MIN),
                    LocalDateTime.of(nowDate.minusDays(week), LocalTime.MAX));
            if (lastWeekRechargeAmount.compareTo(amount) > 0) {
                throw new BizException(ResultCode.LAST_WEEK_RECHARGE_AMOUNT_INSUFFICIENT);
            }
        }

        // 判斷累積充值
        BigDecimal totalRechargeAmount = template.getTotalRechargeAmount();
        if (totalRechargeAmount.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal amount = this.memberTotalRecharge(memberId,
                    template.getTotalRechargeStartTime(),
                    template.getTotalRechargeEndTime());
            if (totalRechargeAmount.compareTo(amount) > 0) {
                throw new BizException(ResultCode.TOTAL_RECHARGE_AMOUNT_INSUFFICIENT);
            }
        }
    }

    /**
     * 取得存送優惠金額
     *
     * @param template
     * @param amount
     */
    public BigDecimal rechargeAwardAmount(MemberRechargeAwardTemplateClientVO template, BigDecimal amount) {
        RechargeAwardModeEnum mode = RechargeAwardModeEnum.getEnum(template.getMode());
        switch (mode) {
            case RATE:
                BigDecimal addAwardAmount = amount.multiply(template.getRate());
                return addAwardAmount.compareTo(template.getMaxAwardAmount()) > 0 ? template.getMaxAwardAmount() : addAwardAmount;
            case FIX:
                return template.getAmount();
            default:
                return BigDecimal.ZERO;
        }
    }

    @SneakyThrows
    @Override
    public void exportOnlineMemberRecharge(ExportOnlineMemberRechargeForm form, HttpServletResponse response) {
        RechargeTimeTypeEnum timeTypeEnum = RechargeTimeTypeEnum.getEnum(form.getTimeType());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        NumberFormat nf = NumberFormat.getInstance();
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern("#.00");

        List<Merchant> merchants = iMerchantService.list();

        List<ExportOnlineMemberRechargeVO> collect = this.lambdaQuery()
                .between(Objects.equals(RechargeTimeTypeEnum.RECEIVE, timeTypeEnum), BaseEntity::getGmtCreate, form.getStartTime(), form.getEndTime())
                .between(Objects.equals(RechargeTimeTypeEnum.TRANSACTION, timeTypeEnum), MemberRecharge::getSuccessTime, form.getStartTime(), form.getEndTime())
                .eq(Objects.nonNull(form.getTradeNo()), MemberRecharge::getTradeNo, form.getTradeNo())
                .eq(StringUtils.isNotBlank(form.getMemberLevelName()), MemberRecharge::getMemberLevelName, form.getMemberLevelName())
                .eq(Objects.nonNull(form.getStatus()), MemberRecharge::getStatus, form.getStatus())
                .eq(Objects.nonNull(form.getUsername()), MemberRecharge::getUsername, form.getUsername())
                .eq(Objects.nonNull(form.getMerchantId()), MemberRecharge::getMerchantId, form.getMerchantId())
                .eq(Objects.nonNull(form.getRechargeTypeId()), MemberRecharge::getRechargeTypeId, form.getRechargeTypeId())
                .in(MemberRecharge::getType, MemberRechargeTypeEnum.ONLINE.getValue(), MemberRechargeTypeEnum.ONLINE_MAKE_UP.getValue())
                .orderByDesc(MemberRecharge::getId)
                .list()
                .stream()
                .map(memberRecharge -> {

                            Merchant merchant = merchants.stream()
                                    .filter(filter -> Objects.equals(filter.getId(), memberRecharge.getMerchantId()))
                                    .findFirst().orElse(Merchant.builder().build());

                            return ExportOnlineMemberRechargeVO.builder()
                                    .tradeNo(memberRecharge.getTradeNo())
                                    .type(MemberRechargeTypeEnum.getEnum(memberRecharge.getType()).getLabel())
                                    .username(memberRecharge.getUsername())
                                    .level(memberRecharge.getMemberLevelName())
                                    .amount(Objects.nonNull(memberRecharge.getAmount()) ? decimalFormat.format(memberRecharge.getAmount().setScale(2, RoundingMode.CEILING)) : String.valueOf(BigDecimal.ZERO.setScale(2)))
                                    .deposit("無")
                                    .fee((Objects.nonNull(memberRecharge.getFee()) && memberRecharge.getFee().compareTo(BigDecimal.ZERO) != 0) ? decimalFormat.format(memberRecharge.getFee().setScale(2, RoundingMode.CEILING)) : String.valueOf(BigDecimal.ZERO.setScale(2)))
                                    .realAmount(Objects.nonNull(memberRecharge.getRealAmount()) ? decimalFormat.format(memberRecharge.getRealAmount().setScale(2, RoundingMode.CEILING)) : String.valueOf(BigDecimal.ZERO.setScale(2)))
                                    .rechargeTypeName(RechargeTypeEnum.getEnum(memberRecharge.getRechargeTypeId()).getLabel())
                                    .bank(memberRecharge.getBank())
                                    .merchantName(merchant.getName())
                                    .status(PayStatusEnum.getEnum(memberRecharge.getStatus()).getLabel())
                                    .remark(memberRecharge.getRemark())
                                    .createTime(memberRecharge.getGmtCreate() != null ? memberRecharge.getGmtCreate().plusHours(form.getGmtTime()).format(dtf) : "--")
                                    .transactionTime(memberRecharge.getSuccessTime() != null ? memberRecharge.getSuccessTime().plusHours(form.getGmtTime()).format(dtf) : "--")
                                    .build();
                        }
                )
                .collect(Collectors.toList());

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = URLEncoder.encode("auto_merchant_" + date, StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcelFactory.write(response.getOutputStream(), ExportOnlineMemberRechargeVO.class)
                .sheet()
                .doWrite(collect);
    }

    @Override
    public Boolean onlineMakeUp(OnlineMakeUpForm form, HttpServletRequest request) {
        Result<MemberInfoDTO> memberInfo = memberFeignClient.getMemberInfo(form.getUsername());
        if (!Result.isSuccess(memberInfo)) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }

        // 取得存送優惠資訊
        Optional<MemberRechargeAwardTemplateClientVO> memberRechargeAwardTemplateOpt = Optional.ofNullable(this.getMemberRechargeAwardTemplateClientVO(form.getRechargeAwardId(), form.getAmount()));

        // 取得會員通道
        MemberChannel memberChannel = Optional.ofNullable(iMemberChannelService.getById(form.getMemberChannelId()))
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        MemberInfoDTO data = memberInfo.getData();

        String orderId = UUIDUtils.genOnlineOrderId();

        MemberRecharge memberRecharge = MemberRecharge.builder()
                .tradeNo(orderId)
                .username(data.getUsername())
                .realName(data.getRealName())
                .memberId(data.getId())
                .memberLevelName(data.getCurrentVipName())
                .amount(form.getAmount())
                .realAmount(form.getAmount().add(memberRechargeAwardTemplateOpt.map(template -> rechargeAwardAmount(template, form.getAmount())).orElse(BigDecimal.ZERO)))
                .merchantId(Long.parseLong(memberChannel.getMerchantId().toString()))
                .memberChannelId(memberChannel.getId())
                .memberChannelName(memberChannel.getChannelName())
                .rechargeTypeId(memberChannel.getRechargeTypeId())
                .rechargeAwardId(form.getRechargeAwardId())
                .rechargeAwardName(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getName).orElse(null))
                .rechargeAwardBetRate(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getBetRate).orElse(null))
                .rechargeAwardAmount(memberRechargeAwardTemplateOpt.map(template -> rechargeAwardAmount(template, form.getAmount())).orElse(null))
                .maxAwardAmount(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getMaxAwardAmount).orElse(null))
                .remark(form.getNote())
                .type(MemberRechargeTypeEnum.ONLINE_MAKE_UP.getValue())
                .status(PayStatusEnum.SUCCESS.getValue())
                .actionIp(ServletUtil.getClientIP(request))
                .successTime(LocalDateTime.now())
                .build();

        boolean save = this.save(memberRecharge);

        if (Boolean.FALSE.equals(save)) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }

        kafkaTemplate.send(BALANCE_CHANGE,
                AddBalanceDTO.builder()
                        .memberId(data.getId())
                        .orderId(orderId)
                        .balance(form.getAmount())
                        .balanceChangeTypeLinkEnum(MAKE_IN_RECHARGE)
                        .type(MAKE_IN_RECHARGE.getType())
                        .betRate(BigDecimal.ONE)
                        .note(MAKE_IN_RECHARGE.getI18n())
                        .rechargeAwardDTO(
                                memberRechargeAwardTemplateOpt.map(template ->
                                                RechargeAwardDTO.builder()
                                                        .templateId(template.getId())
                                                        .name(template.getName())
                                                        .type(template.getType())
                                                        .mode(template.getMode())
                                                        .betRate(template.getBetRate())
                                                        .rate(template.getRate())
                                                        .fixAmount(template.getAmount())
                                                        .minJoinAmount(template.getMinJoinAmount())
                                                        .maxAwardAmount(template.getMaxAwardAmount())
                                                        .build()
                                        )
                                        .orElse(null)
                        )
                        .build()
        );

        if (memberRechargeAwardTemplateOpt.isPresent()) {
            MemberRechargeAwardTemplateClientVO memberRechargeAwardTemplate = memberRechargeAwardTemplateOpt.get();
            kafkaTemplate.send(RECHARGE_AWARD_RECORD,
                    RechargeAwardRecordModel.builder()
                            .action(RechargeAwardRecordModelActionEnum.ADD.getCode())
                            .memberId(data.getId())
                            .username(data.getUsername())
                            .templateId(memberRechargeAwardTemplate.getId())
                            .name(memberRechargeAwardTemplate.getName())
                            .type(memberRechargeAwardTemplate.getType())
                            .mode(memberRechargeAwardTemplate.getMode())
                            .rate(memberRechargeAwardTemplate.getRate())
                            .betRate(memberRechargeAwardTemplate.getBetRate())
                            .amount(memberRechargeAwardTemplate.getAmount())
                            .rechargeAmount(form.getAmount())
                            .useTime(LocalDateTime.now())
                            .state(RechargeAwardRecordStateEnum.USED.getCode())
                            .build()
            );
        }

        // 新增存款成功事件
        MemberRechargeSuccessDTO memberRechargeSuccessDTO = memberRechargeConverter.toMemberRechargeSuccessDTO(memberRecharge);
        RechargeType rechargeTypeById = iRechargeTypeService.getById(memberRechargeSuccessDTO.getRechargeTypeId());
        memberRechargeSuccessDTO.setRechargeTypeI18nKey(rechargeTypeById.getName());
        kafkaTemplate.send(RECHARGE_SUCCESS, memberRechargeSuccessDTO);

        return save;
    }

    @Override
    @Transactional
    public Boolean inlineMakeUp(InlineMakeUpForm form) {
        CompanyBankCard bankCard = iCompanyBankCardService.getById(form.getCompanyCardId());
        checkBankCardDailyMaxAmount(form.getAmount(), bankCard);

        Result<MemberInfoDTO> memberInfo = memberFeignClient.getMemberInfo(form.getUsername());
        if (!Result.isSuccess(memberInfo)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        MemberInfoDTO member = memberInfo.getData();

        // 取得存送優惠資訊
        Optional<MemberRechargeAwardTemplateClientVO> memberRechargeAwardTemplateOpt = Optional.ofNullable(this.getMemberRechargeAwardTemplateClientVO(form.getRechargeAwardId(), form.getAmount()));

        Bank bank = iBankService.getById(bankCard.getBankId());

        String orderId = UUIDUtils.genInlineOrderId();

        // 用戶儲值
        kafkaTemplate.send(BALANCE_CHANGE,
                AddBalanceDTO.builder()
                        .memberId(member.getId())
                        .orderId(orderId)
                        .balance(form.getAmount())
                        .balanceChangeTypeLinkEnum(MAKE_IN_RECHARGE)
                        .type(MAKE_IN_RECHARGE.getType())
                        .betRate(BigDecimal.ONE)
                        .note(MAKE_IN_RECHARGE.getI18n())
                        .rechargeAwardDTO(
                                memberRechargeAwardTemplateOpt.map(template ->
                                                RechargeAwardDTO.builder()
                                                        .templateId(template.getId())
                                                        .name(template.getName())
                                                        .type(template.getType())
                                                        .mode(template.getMode())
                                                        .betRate(template.getBetRate())
                                                        .rate(template.getRate())
                                                        .fixAmount(template.getAmount())
                                                        .minJoinAmount(template.getMinJoinAmount())
                                                        .maxAwardAmount(template.getMaxAwardAmount())
                                                        .build()
                                        )
                                        .orElse(null)
                        )
                        .build()
        );

        MemberRecharge memberRecharge = MemberRecharge.builder()
                .tradeNo(orderId)
                .username(member.getUsername())
                .memberId(member.getId())
                .realName(member.getRealName())
                .memberLevelName(member.getCurrentVipName())
                .amount(form.getAmount())
                .realAmount(form.getAmount().add(memberRechargeAwardTemplateOpt.map(template -> rechargeAwardAmount(template, form.getAmount())).orElse(BigDecimal.ZERO)))
                .rechargeAwardId(form.getRechargeAwardId())
                .rechargeAwardName(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getName).orElse(null))
                .rechargeAwardBetRate(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getBetRate).orElse(null))
                .rechargeAwardAmount(memberRechargeAwardTemplateOpt.map(template -> rechargeAwardAmount(template, form.getAmount())).orElse(null))
                .maxAwardAmount(memberRechargeAwardTemplateOpt.map(MemberRechargeAwardTemplateClientVO::getMaxAwardAmount).orElse(null))
                .bankCardCode(bankCard.getCode())
                .bank(bank.getRechargeName())
                .rechargeTypeId(RechargeTypeEnum.COMPANY_BANK_CARD.getValue())
                .type(MemberRechargeTypeEnum.INLINE_MAKE_UP.getValue())
                .remark(form.getNote())
                .status(PayStatusEnum.SUCCESS.getValue())
                .successTime(LocalDateTime.now())
                .checkUser(UserUtils.getUsername())
                .build();

        // 新增存款成功事件
        MemberRechargeSuccessDTO memberRechargeSuccessDTO = memberRechargeConverter.toMemberRechargeSuccessDTO(memberRecharge);
        RechargeType rechargeTypeById = iRechargeTypeService.getById(memberRechargeSuccessDTO.getRechargeTypeId());
        memberRechargeSuccessDTO.setRechargeTypeI18nKey(rechargeTypeById.getName());
        kafkaTemplate.send(RECHARGE_SUCCESS, memberRechargeSuccessDTO);

        form.setTradeNo(memberRecharge.getTradeNo());

        if (memberRechargeAwardTemplateOpt.isPresent()) {
            MemberRechargeAwardTemplateClientVO memberRechargeAwardTemplate = memberRechargeAwardTemplateOpt.get();
            kafkaTemplate.send(RECHARGE_AWARD_RECORD,
                    RechargeAwardRecordModel.builder()
                            .action(RechargeAwardRecordModelActionEnum.ADD.getCode())
                            .memberId(member.getId())
                            .username(member.getUsername())
                            .templateId(memberRechargeAwardTemplate.getId())
                            .name(memberRechargeAwardTemplate.getName())
                            .type(memberRechargeAwardTemplate.getType())
                            .mode(memberRechargeAwardTemplate.getMode())
                            .rate(memberRechargeAwardTemplate.getRate())
                            .betRate(memberRechargeAwardTemplate.getBetRate())
                            .amount(memberRechargeAwardTemplate.getAmount())
                            .rechargeAmount(form.getAmount())
                            .useTime(LocalDateTime.now())
                            .state(RechargeAwardRecordStateEnum.USED.getCode())
                            .build()
            );
        }

        return this.save(memberRecharge);
    }

    private void checkBankCardDailyMaxAmount(BigDecimal amount, CompanyBankCard bankCard) {
        if (amount.compareTo(bankCard.getMinAmount()) < 0 || amount.compareTo(bankCard.getMaxAmount()) > 0) {
            throw new BizException(ResultCode.RECHARGE_AMOUNT_INVALID);
        }
        String date = LocalDate.now(ZoneId.of("+7")).toString();
        BigDecimal currentAmount;
        Object redisObj = redisTemplate.opsForValue().get(RedisKey.CARD_DAILY_TOTAL_AMOUNT + ":" + bankCard.getCode() + ":" + date);
        if (Objects.isNull(redisObj)) {
            currentAmount = new BigDecimal(0);
        } else {
            currentAmount = new BigDecimal(redisObj.toString());
        }

        if (currentAmount.add(amount).compareTo(bankCard.getDailyMaxAmount()) <= 0) {
            redisTemplate.opsForValue().set(RedisKey.CARD_DAILY_TOTAL_AMOUNT + ":" + bankCard.getCode() + ":" + date,
                    currentAmount.add(amount).toString());
        } else {
            throw new BizException(ResultCode.COMPANY_CARD_ACHIEVE_DAILY_MAX_AMOUNT);
        }
    }

    private CompanyBankCard randomGetCard(List<CompanyBankCard> cardList) {
        String date = LocalDate.now(ZoneId.of("+7")).toString();
        while (cardList.size() > 0) {
            CompanyBankCard card = RandomUtils.getRandomElement(cardList);
            BigDecimal currentAmount;
            Object redisObj = redisTemplate.opsForValue().get(RedisKey.CARD_DAILY_TOTAL_AMOUNT + ":" + card.getCode() + ":" + date);
            if (Objects.isNull(redisObj)) {
                currentAmount = new BigDecimal(0);
                redisTemplate.opsForValue().set(RedisKey.CARD_DAILY_TOTAL_AMOUNT + ":" + card.getCode() + ":" + date, currentAmount.toString());
            } else {
                currentAmount = new BigDecimal(redisObj.toString());
            }

            if (currentAmount.add(card.getMinAmount()).compareTo(card.getDailyMaxAmount()) > 0) {
                cardList.remove(card);
            } else {
                return card;
            }
        }

        return null;
    }

    /**
     * 取得存送優惠模組
     *
     * @param id             模組ID
     * @param rechargeAmount 充值金額
     * @return
     */
    public MemberRechargeAwardTemplateClientVO getMemberRechargeAwardTemplateClientVO(Long id, BigDecimal
            rechargeAmount) {
        if (Objects.nonNull(id)) {
            Result<MemberRechargeAwardTemplateClientVO> rechargeAwardTemplateResult = memberRechargeAwardTemplateClient.findRechargeAwardTemplate(id);
            if (Result.isSuccess(rechargeAwardTemplateResult) && rechargeAwardTemplateResult.getData().getMinJoinAmount().compareTo(rechargeAmount) <= 0) {
                return rechargeAwardTemplateResult.getData();
            }
        }
        return null;
    }

    /**
     * 累積充值要求
     *
     * @param templateId 模組ID
     * @param memberId   會員ID
     * @return
     */
    private Integer getRechargeAwardByTotal(Long templateId, Long memberId) {
        String key = com.c88.common.redis.constant.RedisKey.getRechargeAwardByTotalKey(templateId, memberId);
        Integer num = (Integer) redisTemplate.opsForValue().get(key);
        return Objects.isNull(num) ? 0 : num;
    }

    /**
     * 每月次數
     *
     * @param nowDate    日期
     * @param templateId 模組ID
     * @param memberId   會員ID
     * @return
     */
    private Integer getRechargeAwardByMonth(LocalDate nowDate, Long templateId, Long memberId) {
        String key = com.c88.common.redis.constant.RedisKey.getRechargeAwardByMonthKey(nowDate, templateId, memberId);
        Integer num = (Integer) redisTemplate.opsForValue().get(key);
        return Objects.isNull(num) ? 0 : num;
    }

    /**
     * 每週次數
     *
     * @param templateId 模組ID
     * @param memberId   會員ID
     * @return
     */
    private Integer getRechargeAwardByWeek(Long templateId, Long memberId) {
        String key = com.c88.common.redis.constant.RedisKey.getRechargeAwardByWeekKey(templateId, memberId);
        Integer num = (Integer) redisTemplate.opsForValue().get(key);
        return Objects.isNull(num) ? 0 : num;
    }

    /**
     * 每日次數
     *
     * @param nowDate    日期
     * @param templateId 模組ID
     * @param memberId   會員ID
     * @return
     */
    private Integer getRechargeAwardByDay(LocalDate nowDate, Long templateId, Long memberId) {
        String key = com.c88.common.redis.constant.RedisKey.getRechargeAwardByDayKey(nowDate, templateId, memberId);
        Integer num = (Integer) redisTemplate.opsForValue().get(key);
        return Objects.isNull(num) ? 0 : num;
    }

    /**
     * 檢查存送優惠次數
     *
     * @param memberId 會員ID
     * @param template 存送優惠模組
     */
    private void checkRechargeAwardNum(Long memberId, MemberRechargeAwardTemplateClientVO template) {
        LocalDate nowDate = LocalDate.now();

        // 檢查週日次數
        if (template.getDayNumber() != 0 && template.getDayNumber() <= this.getRechargeAwardByDay(nowDate, template.getId(), memberId)) {
            throw new BizException(ResultCode.RECHARGE_RECHARGE_AWARD_DAY_INVALID);
        }
        // 檢查週次數
        if (template.getWeekNumber() != 0 && template.getWeekNumber() <= this.getRechargeAwardByWeek(template.getId(), memberId)) {
            throw new BizException(ResultCode.RECHARGE_RECHARGE_AWARD_WEEK_INVALID);
        }
        // 檢查月次數
        if (template.getMonthNumber() != 0 && template.getMonthNumber() <= this.getRechargeAwardByMonth(nowDate, template.getId(), memberId)) {
            throw new BizException(ResultCode.RECHARGE_RECHARGE_AWARD_MONTH_INVALID);
        }
        // 檢查累積次數
        if (template.getTotalNumber() != 0 && template.getTotalNumber() <= this.getRechargeAwardByTotal(template.getId(), memberId)) {
            throw new BizException(ResultCode.RECHARGE_RECHARGE_AWARD_TOTAL_INVALID);
        }

    }

    /**
     * 檢查存送優惠充值與次數
     *
     * @param memberRechargeAwardTemplateOpt 存送優惠模組Opt
     * @param memberId                       會員ID
     */
    private void checkRechargeAward
    (Optional<MemberRechargeAwardTemplateClientVO> memberRechargeAwardTemplateOpt, Long memberId) {
        memberRechargeAwardTemplateOpt.ifPresent(memberRechargeAwardTemplate -> {
                    // 檢查個人存送優惠使用狀態
                    if (Objects.equals(memberRechargeAwardTemplate.getType(), RechargeAwardTypeEnum.PERSONAL.getCode())) {
                        Result<List<AllMemberPersonalRechargeAwardRecordByTemplateIdVO>> personalRechargeAwardRecordResult = memberRechargeAwardRecordClient.findAllPersonalRechargeAwardRecordByTemplateId(memberRechargeAwardTemplate.getId(), memberId);
                        if (!Result.isSuccess(personalRechargeAwardRecordResult) || personalRechargeAwardRecordResult.getData()
                                .stream()
                                .noneMatch(record -> Objects.equals(record.getState(), RechargeAwardRecordStateEnum.UNUSED.getCode()))
                        ) {
                            throw new BizException(ResultCode.RECHARGE_AWARD_PERSONAL_CANCEL);
                        }
                    }

                    // 檢查上週,昨日,累積充值
                    checkRechargeAwardAmount(memberId, memberRechargeAwardTemplate);

                    // 檢查日,周,月,累次數
                    checkRechargeAwardNum(memberId, memberRechargeAwardTemplate);

                    // 減少優惠使用次數
                    LocalDate nowDate = LocalDate.now();
                    redisTemplate.opsForValue().increment(getRechargeAwardByDayKey(nowDate, memberRechargeAwardTemplate.getId(), memberId));
                    redisTemplate.opsForValue().increment(getRechargeAwardByWeekKey(memberRechargeAwardTemplate.getId(), memberId));
                    redisTemplate.opsForValue().increment(getRechargeAwardByMonthKey(nowDate, memberRechargeAwardTemplate.getId(), memberId));
                    redisTemplate.opsForValue().increment(getRechargeAwardByTotalKey(memberRechargeAwardTemplate.getId(), memberId));
                }
        );
    }

    @Override
    public IPage<MemberDepositDTO> findMemberRechargeFromAffiliate(SearchMemberDepositForm form) {
        Map<String, RechargeType> rechargeTypesMap = iRechargeTypeService.findRechargeTypeMap();
        LocalDateTime beginTime = form.getBeginTime();
        LocalDateTime endTime = form.getEndTime();
        return this.lambdaQuery().eq(MemberRecharge::getUsername, form.getUsername())
                .ge(beginTime != null, MemberRecharge::getCreateTime, beginTime)
                .le(endTime != null, MemberRecharge::getCreateTime, endTime)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(x -> memberRechargeConverter.toDepositDTO(x, rechargeTypesMap));
    }

    @Override
    public List<RechargeWithdrawReportVO> getSuccessRechargeCount(Integer status, List<Integer> types, String startTime, String endTime) {
        QueryWrapper<RechargeWithdrawReportVO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        queryWrapper.in("type", types);
        queryWrapper.ge("success_time", startTime);
        queryWrapper.lt("success_time", endTime);
        queryWrapper.groupBy("username");
        return rechargeMapper.getSuccessRechargeCount(status, types, startTime, endTime, queryWrapper);
    }

    @Override
    public List<Long> getRechargeMembers(String startTime, String endTime) {
        return this.lambdaQuery().gt(MemberRecharge::getSuccessTime, startTime).lt(MemberRecharge::getSuccessTime, endTime).groupBy(MemberRecharge::getMemberId).list()
                .stream().mapToLong(MemberRecharge::getMemberId).boxed().collect(Collectors.toList());
    }

    @Override
    public BigDecimal getFee(String startTime, String endTime) {
        //iBetOrderService.getBaseMapper().selectOne(new QueryWrapper<BetOrder>()
        //                .select("sum(valid_bet_amount) as validBetAmount")
        //                .lambda()
        MemberRecharge memberRecharge = this.getBaseMapper().selectOne(new QueryWrapper<MemberRecharge>()
                .select(" ifnull(sum(fee),0) as fee ")
                .lambda()
                .gt(MemberRecharge::getSuccessTime, startTime)
                .lt(MemberRecharge::getSuccessTime, endTime)
                .eq(MemberRecharge::getStatus, 1));
        return memberRecharge == null ? BigDecimal.ZERO : memberRecharge.getFee();
    }
}


package com.c88.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.admin.api.TagFeignClient;
import com.c88.admin.dto.TagDTO;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.enums.SortTypeEnum;
import com.c88.common.web.exception.BizException;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.member.vo.OptionVO;
import com.c88.payment.constants.RedisKey;
import com.c88.payment.enums.*;
import com.c88.payment.mapper.MemberChannelGroupMapper;
import com.c88.payment.mapper.MemberChannelMapper;
import com.c88.payment.mapstruct.MemberChannelConverter;
import com.c88.payment.mapstruct.MemberChannelGroupConverter;
import com.c88.payment.pojo.entity.*;
import com.c88.payment.pojo.form.*;
import com.c88.payment.pojo.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberChannelCommonService {

    public static final int FIRST_SORT = 1;

    private final IChannelTagService channelTagService;

    private final IMemberChannelGroupService iMemberChannelGroupService;

    private final IMemberChannelVipConfigMappingService iMemberChannelVipConfigMappingService;

    private final TagFeignClient tagFeignClient;

    private final MemberFeignClient memberFeignClient;

    private final IMerchantService iMerchantService;

    private final MemberChannelConverter memberChannelConverter;

    private final IMerchantRechargeTypeService iMerchantRechargeTypeService;

    private final IMerchantRechargeChannelService iMerchantRechargeChannelService;

    private final IBankService iBankService;

    private final IMemberChannelService iMemberChannelService;

    private final MemberChannelMapper memberChannelMapper;

    private final MemberChannelGroupMapper memberChannelGroupMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    private final MemberChannelGroupConverter memberChannelGroupConverter;

    private final ICompanyBankCardGroupService iCompanyBankCardGroupService;

    private final IMemberChannelVipConfigSettingService iMemberChannelVipConfigSettingService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.USAGE_RECHARGE_TYPE, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.CHOOSE_CHANNEL_QUEUE, allEntries = true)
    })
    public Boolean insertMemberChannel(MemberChannelForm form) {
        if (Objects.nonNull(form.getMerchantId())) {
            Merchant merchant = iMerchantService.lambdaQuery()
                    .select(Merchant::getCode, Merchant::getName)
                    .eq(Merchant::getId, form.getMerchantId())
                    .one();
            form.setMerchantCode(merchant.getCode());
            form.setMerchantName(merchant.getName());
        }

        MemberChannel memberChannel = memberChannelConverter.toEntity(form);

        Boolean result = iMemberChannelService.save(memberChannel);
        form.setId(memberChannel.getId());// 操作日誌，記錄需要，請勿移除

        //新增對應的群組
        AtomicInteger index = new AtomicInteger(FIRST_SORT);
        List<MemberChannelGroup> groupList = memberChannel.getVipIds().stream().map(vipConfigId -> {
            MemberChannelGroup memberChannelGroup = new MemberChannelGroup();
            memberChannelGroup.setVipConfigId(vipConfigId);
            memberChannelGroup.setMemberChanelId(memberChannel.getId());
            memberChannelGroup.setRechargeTypeId(memberChannel.getRechargeTypeId() == null ? 0 : memberChannel.getRechargeTypeId());
            memberChannelGroup.setSort(index.getAndIncrement());
            return memberChannelGroup;
        }).collect(Collectors.toList());
        iMemberChannelGroupService.saveBatch(groupList);

        //channelTag轉換
        List<ChannelTag> tagList = form.getMemberTags()
                .stream()
                .map(tagId -> new ChannelTag(memberChannel.getId(), tagId))
                .collect(Collectors.toList());
        channelTagService.saveBatch(tagList);

        //vipConfig 轉換
        List<MemberChannelVipConfigMapping> memberChannelVipConfigMappingList = form.getVipIds()
                .stream()
                .map(vipConfigId -> {
                    MemberChannelVipConfigMapping memberChannelVipConfigMapping = new MemberChannelVipConfigMapping();
                    memberChannelVipConfigMapping.setMemberChannelId(memberChannel.getId());
                    memberChannelVipConfigMapping.setVipConfigId(vipConfigId);
                    return memberChannelVipConfigMapping;
                }).collect(Collectors.toList());
        iMemberChannelVipConfigMappingService.saveBatch(memberChannelVipConfigMappingList);
        return result;
    }

    @Transactional
    public IPage<MemberChannelVO> findMemberChannels(FindMemberChannelForm form) {
        List<MemberChannel> memberChannels = iMemberChannelService.lambdaQuery()
                .eq(Objects.nonNull(form.getId()), MemberChannel::getId, form.getId())
                .eq(Objects.nonNull(form.getMerchantId()), MemberChannel::getMerchantId, form.getMerchantId())
                .eq(Objects.nonNull(form.getRechargeTypeId()), MemberChannel::getRechargeTypeId, form.getRechargeTypeId())
                .eq(Objects.nonNull(form.getCompanyBankCardGroupId()), MemberChannel::getCompanyBankCardGroupId, form.getCompanyBankCardGroupId())
                .eq(Objects.nonNull(form.getStatus()), MemberChannel::getStatus, form.getStatus())
                .eq(StringUtils.isNotBlank(form.getRechargeShowName()), MemberChannel::getRechargeShowName, form.getRechargeShowName())
                .eq(StringUtils.isNotBlank(form.getChannelName()), MemberChannel::getChannelName, form.getChannelName())
                .apply(Objects.nonNull(form.getMemberVip()), "FIND_IN_SET('"+form.getMemberVip()+"', vip_ids)")
                .apply(Objects.nonNull(form.getMemberTag()), "FIND_IN_SET('"+form.getMemberTag()+"', member_tags)")
//                .like(Objects.nonNull(form.getMemberVip()), MemberChannel::getVipIds, form.getMemberVip())
//                .like(Objects.nonNull(form.getMemberTag()), MemberChannel::getMemberTags, form.getMemberTag())
                .orderByDesc(MemberChannel::getId)
                .list();

        if (CollectionUtils.isEmpty(memberChannels)) {
            return new Page<>(form.getPageNum(), form.getPageSize());
        }

        // 取得商戶ByID
        List<Merchant> merchants = iMerchantService.list();

        //取得商戶群組
        List<MerchantRechargeType> merchantRechargeTypes = iMerchantRechargeTypeService.list();

        // 取得通道
        List<MerchantRechargeChannel> merchantRechargeChannels = iMerchantRechargeChannelService.list();

        // 取得銀行
        List<Bank> banks = iBankService.list();

        // 取得自營卡群組
        List<CompanyBankCardGroup> companyBankCardGroups = iCompanyBankCardGroupService.list();

        // 取得會員標籤
        List<TagDTO> tags = new ArrayList<>();
        Result<List<TagDTO>> tagResult = tagFeignClient.listTags();
        if (Result.isSuccess(tagResult)) {
            tags = tagResult.getData();
        }

        // 取得會員等級
        Map<Integer, String> vipConfigMap = new HashMap<>();
        Result<Map<Integer, String>> memberVipConfigResult = memberFeignClient.findMemberVipConfigMap();
        if (Result.isSuccess(memberVipConfigResult)) {
            vipConfigMap.putAll(memberVipConfigResult.getData());
        }

        List<TagDTO> finalTags = tags;
        List<MemberChannelVO> memberChannelVOS = memberChannels.stream()
                .map(memberChannel -> {
                            // 取得tag名稱
                            List<TagVO> tagVOS = finalTags.stream()
                                    .filter(filter -> memberChannel.getMemberTags().contains(filter.getId().longValue()))
                                    .map(tag -> TagVO.builder().id(tag.getId()).name(tag.getName()).build())
                                    .collect(Collectors.toList());

                            // 取得VIP名稱
                            List<VipVO> vipVOS = memberChannel.getVipIds()
                                    .stream()
                                    .map(vipId -> VipVO.builder().id(vipId.intValue()).name(vipConfigMap.get(vipId.intValue())).build())
                                    .collect(Collectors.toList());

                            MemberChannelVO memberChannelVO = memberChannelConverter.toVo(memberChannel);
                            memberChannelVO.setMemberTagVOs(tagVOS);
                            memberChannelVO.setMemberVipVOs(vipVOS);
                            memberChannelVO.setMerchantStatus(getMerchantStatus(merchants,
                                    merchantRechargeTypes, merchantRechargeChannels, banks, memberChannel).getValue());

                            // 寫入支付方式名稱
                            if (Objects.nonNull(memberChannel.getCompanyBankCardGroupId())) {
                                companyBankCardGroups.stream()
                                        .filter(filter -> Objects.equals(filter.getId(), memberChannel.getCompanyBankCardGroupId()))
                                        .findFirst()
                                        .ifPresent(companyBankCardGroup -> memberChannelVO.setRechargeTypeName(companyBankCardGroup.getName()));
                            } else {
                                merchantRechargeTypes.stream()
                                        .filter(filter -> Objects.equals(filter.getRechargeTypeId().intValue(), memberChannel.getRechargeTypeId()))
                                        .findFirst()
                                        .ifPresent(merchantRechargeType -> memberChannelVO.setRechargeTypeName(merchantRechargeType.getRechargeTypeName()));
                            }

                            return memberChannelVO;
                        }
                )
                .filter(filter -> Objects.isNull(form.getMerchantStatus()) || Objects.equals(filter.getMerchantStatus(), form.getMerchantStatus()))
                .collect(Collectors.toList());

        // 放入page
        Page<MemberChannelVO> memberChannelVOPage = new Page<>(form.getPageNum(), form.getPageSize());
        memberChannelVOPage.setRecords(
                memberChannelVOS.stream()
                        .skip((long) (form.getPageNum() - 1) * form.getPageSize())
                        .limit(form.getPageSize())
                        .collect(Collectors.toList())
        );
        memberChannelVOPage.setTotal(memberChannelVOS.size());
        memberChannelVOPage.setPages(memberChannelVOS.size() / form.getPageSize());

        return memberChannelVOPage;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.USAGE_RECHARGE_TYPE, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.CHOOSE_CHANNEL_QUEUE, allEntries = true)
    })
    public Boolean updateMemberChannel(MemberChannelForm form, boolean isOnlyUpdateStatus) {
        if (!isOnlyUpdateStatus && CollectionUtils.isEmpty(form.getVipIds())) {
            throw new BizException(ResultCode.PARAM_IS_NULL);
        }
        MemberChannel memberChannel = memberChannelConverter.toEntity(form);
        Boolean result = iMemberChannelService.updateById(memberChannel);
        if (form.getType() == null) {
            return true;
        }
        iMemberChannelGroupService.remove(new LambdaQueryWrapper<MemberChannelGroup>().eq(MemberChannelGroup::getMemberChanelId, memberChannel.getId()));
        //新增對應的群組
        AtomicInteger index = new AtomicInteger(FIRST_SORT);
        List<MemberChannelGroup> groupList = memberChannel.getVipIds().stream().map(vipConfigId -> {
            MemberChannelGroup memberChannelGroup = new MemberChannelGroup();
            memberChannelGroup.setVipConfigId(vipConfigId);
            memberChannelGroup.setMemberChanelId(memberChannel.getId());
            memberChannelGroup.setRechargeTypeId(memberChannel.getRechargeTypeId());
            memberChannelGroup.setSort(index.getAndIncrement());
            return memberChannelGroup;
        }).collect(Collectors.toList());
        iMemberChannelGroupService.saveBatch(groupList);

        channelTagService.remove(new LambdaQueryWrapper<ChannelTag>().eq(ChannelTag::getChannelId, memberChannel.getId()));
        List<ChannelTag> tagList = form.getMemberTags()
                .stream()
                .map(tagId -> new ChannelTag(memberChannel.getId(), tagId)).collect(Collectors.toList());
        channelTagService.saveBatch(tagList);

        iMemberChannelVipConfigMappingService.remove(new LambdaQueryWrapper<MemberChannelVipConfigMapping>().eq(MemberChannelVipConfigMapping::getMemberChannelId, memberChannel.getId()));
        List<MemberChannelVipConfigMapping> memberChannelVipConfigMappingList = form.getVipIds()
                .stream()
                .map(vipConfigId -> {
                    redisTemplate.delete(RedisKey.getChooseChannelQueueKey(vipConfigId.intValue(), form.getRechargeTypeId()));

                    MemberChannelVipConfigMapping memberChannelVipConfigMapping = new MemberChannelVipConfigMapping();
                    memberChannelVipConfigMapping.setMemberChannelId(memberChannel.getId());
                    memberChannelVipConfigMapping.setVipConfigId(vipConfigId);
                    return memberChannelVipConfigMapping;
                }).collect(Collectors.toList());
        iMemberChannelVipConfigMappingService.saveBatch(memberChannelVipConfigMappingList);

        return result;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.USAGE_RECHARGE_TYPE, allEntries = true),
            @CacheEvict(cacheNames = RedisKey.CHOOSE_CHANNEL_QUEUE, allEntries = true)
    })
    public Boolean delMemberChannel(Long channelId) {
        iMemberChannelGroupService.remove(new LambdaQueryWrapper<MemberChannelGroup>().eq(MemberChannelGroup::getMemberChanelId, channelId));
        return iMemberChannelService.removeById(channelId);
    }

    //    @Cacheable(cacheNames = RedisKey.USAGE_RECHARGE_TYPE, key = "#memberInfoDTO.currentVipId+':'+#rechargeTypeId")
    public List<MemberChannel> findUsableMemberChannel(MemberInfoDTO memberInfoDTO, Integer rechargeTypeId) {
        return memberChannelMapper.findUsableMemberChannel(memberInfoDTO, rechargeTypeId);
    }

    public List<MemberChannel> findUsableMemberChannelByCompany(MemberInfoDTO memberInfoDTO, Integer rechargeTypeId) {
        return memberChannelMapper.findUsableMemberChannelByCompany(memberInfoDTO, rechargeTypeId);
    }

    public List<MemberChannelRedis> findMemberChannel(Integer vipId, Integer rechargeTypeId) {
        return memberChannelMapper.findMemberChannel(vipId, rechargeTypeId);
    }

    public List<OptionVO<Long>> findMemberChannelOption() {
        return iMemberChannelService.list()
                .stream()
                .filter(filter -> !RechargeTypeEnum.COMPANY_BANK_CARD.getValue().equals(filter.getRechargeTypeId()))
                .map(memberChannel -> OptionVO.<Long>builder()
                        .value(memberChannel.getId())
                        .label(memberChannel.getChannelName())
                        .build()
                )
                .collect(Collectors.toUnmodifiableList());
    }

    public MerchantStatusEnum getMerchantStatus(List<Merchant> merchants,
                                                List<MerchantRechargeType> merchantRechargeTypes,
                                                List<MerchantRechargeChannel> merchantRechargeChannels,
                                                List<Bank> banks, MemberChannel memberChannel) {
        // 預設商戶狀態為開啟
        MerchantStatusEnum merchantStatusEnum = MerchantStatusEnum.MERCHANT_OPEN;

        // 判斷為自營卡直接回傳
        if (Objects.equals(PayTypeEnum.COMPANY_CARD.getValue(), memberChannel.getType())) {
            return merchantStatusEnum;
        }

        // 取得商戶
        Merchant merchant = merchants.stream()
                .filter(filter -> Objects.equals(filter.getId().intValue(), memberChannel.getMerchantId()))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        // 判斷商戶是否停用
        if (Objects.equals(merchant.getEnable(), EnableEnum.STOP.getCode())) {
            return MerchantStatusEnum.MERCHANT_CLOSE;
        }

        // 取得商戶對應的充值類型
        MerchantRechargeType merchantRechargeType = merchantRechargeTypes.stream()
                .filter(filter -> Objects.equals(filter.getMerchantId().intValue(), memberChannel.getMerchantId()) && Objects.equals(filter.getRechargeTypeId().intValue(), memberChannel.getRechargeTypeId()))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        // 判斷充值類型是否停用
        if (Objects.equals(merchantRechargeType.getEnable(), EnableEnum.STOP.getCode())) {
            return MerchantStatusEnum.RECHARGE_TYPE_STOP;
        }

        // 判斷如果底下有銀行則繼續判斷
        if (Objects.equals(merchantRechargeType.getIsBank(), MerchantRechargeTypeIsBank.NOT_BANK.getCode())) {
            return merchantStatusEnum;
        }

        // 商戶底下支付方式的通道
        List<MerchantRechargeChannel> channels = merchantRechargeChannels.stream()
                .filter(filter -> Objects.equals(filter.getMerchantId(), memberChannel.getMerchantId()) && Objects.equals(filter.getRechargeTypeId(), memberChannel.getRechargeTypeId()))
                .collect(Collectors.toList());

        // 判斷銀行停用狀態
        Set<Integer> channelEnables = channels.stream()
                .filter(filter -> Objects.equals(filter.getRechargeTypeId(), merchantRechargeType.getRechargeTypeId().intValue()))
                .map(MerchantRechargeChannel::getEnable)
                .collect(Collectors.toSet());

        // 判斷維護是否有啟用與停用
        if (channelEnables.size() == 2) {
            return MerchantStatusEnum.SOME_BANK_STOP;
        } else {
            // 判斷單一狀態是否為停用
            if (channelEnables.stream().anyMatch(filter -> Objects.equals(filter, EnableEnum.STOP.getCode()))) {
                return MerchantStatusEnum.ALL_BANK_STOP;
            }
        }

        // 判斷銀行維護狀態
        Set<Long> nowBankIds = channels.stream()
                .map(MerchantRechargeChannel::getBankId)
                .collect(Collectors.toSet());

        List<Bank> nowBanks = banks.stream()
                .filter(filter -> nowBankIds.contains(filter.getId()))
                .collect(Collectors.toList());

        Set<Integer> bankStates = nowBanks.stream()
                .map(Bank::getState)
                .collect(Collectors.toSet());

        if (bankStates.size() > 1) {
            if (bankStates.stream().anyMatch(filter -> Objects.equals(filter, BankStateEnum.MAINTAINING.getValue()))) {
                return MerchantStatusEnum.SOME_BANK_MAINTAIN;
            }
        } else if (bankStates.stream().anyMatch(filter -> Objects.equals(filter, BankStateEnum.MAINTAINING.getValue()))) {
            return MerchantStatusEnum.ALL_BANK_MAINTAIN;
        }

        return merchantStatusEnum;
    }

    public IPage<MemberChannelGroupVO> findMemberChannelGroup(FindMemberChannelGroupForm form) {
        List<MemberChannel> memberChannels = iMemberChannelService.lambdaQuery()
                .eq(MemberChannel::getRechargeTypeId, form.getRechargeTypeId())
                .list();

        // 過濾vip
        memberChannels = memberChannels.stream()
                .filter(filter -> filter.getVipIds().contains(form.getVipId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(memberChannels)) {
            return new Page<>(form.getPageNum(), form.getPageSize());
        }

        // 取得商戶
        List<Merchant> merchants = iMerchantService.list();

        //取得商戶群組
        List<MerchantRechargeType> merchantRechargeTypes = iMerchantRechargeTypeService.list();

        // 取得通道
        List<MerchantRechargeChannel> merchantRechargeChannels = iMerchantRechargeChannelService.list();

        // 取得銀行
        List<Bank> banks = iBankService.list();

        // 取得自營卡群組
        List<CompanyBankCardGroup> companyBankCardGroups = iCompanyBankCardGroupService.list();

        List<VipConfigSettingLabelVO> list = iMemberChannelVipConfigSettingService.getVipConfigSettingLabel(form.getVipId().intValue());

        Map<Integer, VipConfigSettingLabelVO> vipConfigSettingLabelVOMap = list.stream()
                .collect(Collectors.toMap(VipConfigSettingLabelVO::getRechargeTypeId, Function.identity()));

        // 取得會員通道群組by支付類型和VIP
        List<MemberChannelGroup> memberChannelGroups = iMemberChannelGroupService.lambdaQuery()
                .eq(MemberChannelGroup::getRechargeTypeId, form.getRechargeTypeId())
                .eq(MemberChannelGroup::getVipConfigId, form.getVipId())
                .list();

        // 取得會員通道群組指派方式
        String key = RedisKey.getChooseMemberChannelStrategyKey(form.getVipId().intValue(), form.getRechargeTypeId());
        Integer random = (Integer) redisTemplate.opsForValue().get(key);
        if (Objects.isNull(random)) {
            random = AssignTypeEnum.ROTATION.getValue();
        }

        Integer finalRandom = random;
        List<MemberChannelGroupVO> memberChannelGroupVOS = memberChannels.stream()
                .map(memberChannel -> {

                            MemberChannelGroup memberChannelGroup = memberChannelGroups.stream()
                                    .filter(filter -> Objects.equals(filter.getMemberChanelId(), memberChannel.getId()))
                                    .findFirst()
                                    .orElse(MemberChannelGroup.builder().build());

                            MemberChannelGroupVO memberChannelGroupVO = memberChannelGroupConverter.toVo(memberChannel);
                            memberChannelGroupVO.setChannelGroupId(memberChannelGroup.getId());
                            memberChannelGroupVO.setSort(memberChannelGroup.getSort());
                            memberChannelGroupVO.setLabel(vipConfigSettingLabelVOMap.getOrDefault(memberChannel.getRechargeTypeId(), new VipConfigSettingLabelVO()).getLabel());
                            memberChannelGroupVO.setAssignmentType(finalRandom);
                            memberChannelGroupVO.setMerchantStatus(
                                    getMerchantStatus(
                                            merchants,
                                            merchantRechargeTypes,
                                            merchantRechargeChannels,
                                            banks,
                                            memberChannel).getValue()
                            );

                            // 寫入支付方式名稱
                            if (Objects.nonNull(memberChannel.getCompanyBankCardGroupId())) {
                                companyBankCardGroups.stream()
                                        .filter(filter -> Objects.equals(filter.getId(), memberChannel.getCompanyBankCardGroupId()))
                                        .findFirst()
                                        .ifPresent(companyBankCardGroup -> memberChannelGroupVO.setRechargeTypeName(companyBankCardGroup.getName()));
                            } else {
                                merchantRechargeTypes.stream()
                                        .filter(filter -> Objects.equals(filter.getRechargeTypeId().intValue(), memberChannel.getRechargeTypeId()))
                                        .findFirst()
                                        .ifPresent(merchantRechargeType -> memberChannelGroupVO.setRechargeTypeName(merchantRechargeType.getRechargeTypeName()));
                            }

                            return memberChannelGroupVO;
                        }
                )
                .sorted(Comparator.comparing(MemberChannelGroupVO::getSort)).collect(Collectors.toList());

        // 放入page
        Page<MemberChannelGroupVO> memberChannelGroupVOPage = new Page<>(form.getPageNum(), form.getPageSize());
        memberChannelGroupVOPage.setRecords(
                memberChannelGroupVOS.stream()
                        .skip((long) (form.getPageNum() - 1) * form.getPageSize())
                        .limit(form.getPageSize())
                        .collect(Collectors.toList())
        );
        memberChannelGroupVOPage.setTotal(memberChannelGroupVOS.size());
        memberChannelGroupVOPage.setPages(memberChannelGroupVOS.size() / form.getPageSize());

        return memberChannelGroupVOPage;
    }

    public Boolean modifyAssign(ModifyMemberChannelGroupAssignForm form) {
        try {
            String strategyKey = RedisKey.getChooseMemberChannelStrategyKey(form.getVipId(), form.getRechargeTypeId());
            String queueKey = RedisKey.getChooseChannelQueueKey(form.getVipId(), form.getRechargeTypeId());
            redisTemplate.opsForValue().set(strategyKey, form.getType());
            redisTemplate.delete(queueKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean modifyMemberChannelGroupSort(ModifyMemberChannelGroupSortForm form) {
        return iMemberChannelGroupService.updateBatchById(
                form.getMemberChannelGroupSortForm()
                        .stream()
                        .map(x ->
                                MemberChannelGroup.builder()
                                        .id(x.getId())
                                        .sort(x.getSort())
                                        .build()
                        )
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public Boolean modifyMemberChannelGroupTopBottom(ModifyMemberChannelGroupSortTopBottomForm form) {
        SortTypeEnum sortType = SortTypeEnum.getEnum(form.getSortType());
        switch (sortType) {
            case TOP:
                return this.memberChannelGroupMapper.modifyMemberChannelGroupTop(form.getId());
            case BOTTOM:
                return this.memberChannelGroupMapper.modifyMemberChannelGroupBottom(form.getId());
            default:
                return Boolean.FALSE;
        }
    }

    public MemberChannel getById(Long id) {
        return iMemberChannelService.getById(id);
    }

    public Set<Long> findMemberChannelUseVipIds() {
        return iMemberChannelService.lambdaQuery()
                .select(MemberChannel::getVipIds)
                .list()
                .stream()
                .map(MemberChannel::getVipIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}

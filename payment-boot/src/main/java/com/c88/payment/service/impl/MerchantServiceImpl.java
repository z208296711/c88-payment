package com.c88.payment.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.common.core.constant.GlobalConstants;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;

import com.c88.member.vo.OptionVO;
import com.c88.payment.constants.RedisKey;
import com.c88.payment.mapper.MerchantMapper;
import com.c88.payment.mapstruct.MerchantConverter;
import com.c88.payment.pojo.dto.BasePayDTO;
import com.c88.payment.pojo.entity.*;
import com.c88.payment.pojo.form.CheckChannelForm;
import com.c88.payment.pojo.form.FindCheckChannelForm;
import com.c88.payment.pojo.form.FindMerchantPageForm;
import com.c88.payment.pojo.form.ModifyMerchantNoteForm;
import com.c88.payment.pojo.vo.*;
import com.c88.payment.service.*;
import com.c88.payment.service.thirdparty.IThirdPartPayService;
import com.c88.payment.service.thirdparty.ThirdPartPaymentExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant>
        implements IMerchantService {

    private final MerchantConverter merchantConverter;

    private final IMerchantRechargeChannelService iMerchantRechargeChannelService;

    private final IRechargeTypeService iRechargeTypeService;

    private final IBankService iBankService;

    private final IMerchantRechargeTypeService iMerchantRechargeTypeService;

    private final ICompanyBankCardGroupService iCompanyBankCardGroupService;

    private final ThirdPartPaymentExecutor thirdPartPaymentExecutor;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Merchant findByCode(String code) {
        return this.lambdaQuery().eq(Merchant::getCode, code).one();
    }

    @Override
    public Merchant getMerchantById(Long id) {
        return Optional.ofNullable(this.getById(id))
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public IPage<MerchantVO> findMerchantPage(FindMerchantPageForm form) {
        return this.lambdaQuery()
                .eq(form.getEnable() != null, Merchant::getEnable, form.getEnable())
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(merchantConverter::toVo);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "payment", key = "'rechargeTypes'", allEntries = true),
            @CacheEvict(cacheNames = RedisKey.USAGE_RECHARGE_TYPE, allEntries = true)
        })
    public Boolean modifyMerchant(ModifyMerchantNoteForm form) {
        Set<String> chooseChannelQueue = redisTemplate.keys("chooseChannelQueue*");
        redisTemplate.delete(chooseChannelQueue);
        Set<String> chooseChannelStrategy = redisTemplate.keys("chooseChannelStrategy*");
        redisTemplate.delete(chooseChannelStrategy);
        return this.lambdaUpdate()
                .eq(Merchant::getId, form.getId())
                .set(Objects.nonNull(form.getNote()), Merchant::getNote, form.getNote())
                .set(Objects.nonNull(form.getEnable()), Merchant::getEnable, form.getEnable())
                .update();
    }

    @Override
    public MerchantStateSettingVO findMerchantState(Integer merchantId) {
        Merchant merchant = Optional.ofNullable(this.getById(merchantId))
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        List<Bank> banks = iBankService.list();

        List<MerchantRechargeType> merchantRechargeTypes = iMerchantRechargeTypeService.lambdaQuery()
                .eq(MerchantRechargeType::getMerchantId, merchantId)
                .list();

        List<MerchantRechargeChannel> merchantRechargeChannels = iMerchantRechargeChannelService.lambdaQuery()
                .eq(MerchantRechargeChannel::getMerchantId, merchant.getId())
                .list();

        Map<Integer, List<MerchantRechargeChannel>> merchantRechargeChannelByMerchantRechargeTypeId = merchantRechargeChannels.stream()
                .collect(Collectors.groupingBy(MerchantRechargeChannel::getRechargeTypeId));

        List<MerchantRechargeTypeSettingVO> merchantRechargeTypeSettingVOS = merchantRechargeChannelByMerchantRechargeTypeId.entrySet()
                .stream()
                .map(m -> {
                            Long rechargeTypeId = Long.parseLong(m.getKey().toString());
                            List<MerchantRechargeChannel> value = m.getValue();

                            MerchantRechargeType merchantRechargeType = merchantRechargeTypes.stream()
                                    .filter(filter -> Objects.equals(filter.getRechargeTypeId(), rechargeTypeId))
                                    .findFirst()
                                    .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

                            List<MerchantRechargeChannelSettingVO> merchantRechargeChannelSettingVOS = value.stream()
                                    .map(x ->
                                            {
                                                Bank bank = banks.stream()
                                                        .filter(filter -> Objects.equals(filter.getId(), x.getBankId()))
                                                        .findFirst()
                                                        .orElse(Bank.builder().id(0L).name("無銀行").build());

                                                return MerchantRechargeChannelSettingVO.builder()
                                                        .id(x.getId())
                                                        .name(bank.getName())
                                                        .note(bank.getNote())
                                                        .enable(x.getEnable())
                                                        .layoutType(3)
                                                        .build();
                                            }
                                    )
                                    .collect(Collectors.toList());

                            return MerchantRechargeTypeSettingVO.builder()
                                    .id(merchantRechargeType.getId())
                                    .name(merchantRechargeType.getRechargeTypeName())
                                    .note(merchantRechargeType.getNote())
                                    .enable(merchantRechargeType.getEnable())
                                    .settings(merchantRechargeChannelSettingVOS)
                                    .layoutType(2)
                                    .build();
                        }
                )
                .collect(Collectors.toList());

        return MerchantStateSettingVO.builder()
                .id(merchantId)
                .name(merchant.getName())
                .enable(merchant.getEnable())
                .settings(merchantRechargeTypeSettingVOS)
                .layoutType(1)
                .build();

    }

    @Override
    public List<MerchantSettingOptionVO> findMerchantSettingOption() {
        List<MerchantRechargeChannel> allMerchantRechargeChannels = iMerchantRechargeChannelService.list();

        List<RechargeType> rechargeTypes = iRechargeTypeService.list();

        return this.lambdaQuery()
                .eq(Merchant::getEnable, GlobalConstants.STATUS_YES)
                .select(Merchant::getId, Merchant::getCode)
                .list()
                .stream()
                .map(merchant -> {
                            List<OptionVO<Integer>> collect = allMerchantRechargeChannels.stream()
                                    .filter(filter -> Objects.equals(filter.getMerchantId(), merchant.getId().intValue()))
                                    .map(merchantRechargeChannel -> {
                                                RechargeType rechargeType = rechargeTypes.stream()
                                                        .filter(filter -> Objects.equals(filter.getId(), merchantRechargeChannel.getRechargeTypeId()))
                                                        .findFirst()
                                                        .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

                                                return OptionVO.<Integer>builder()
                                                        .value(merchantRechargeChannel.getRechargeTypeId())
                                                        .label(rechargeType.getName())
                                                        .build();
                                            }
                                    )
                                    .collect(Collectors.toUnmodifiableList());

                            return MerchantSettingOptionVO.builder()
                                    .id(merchant.getId())
                                    .code(merchant.getCode())
                                    .rechargeTypes(collect)
                                    .build();
                        }
                )
                .collect(Collectors.toList());
    }

    @Override
    public Boolean checkChannel(CheckChannelForm form, HttpServletRequest request) {
        MerchantRechargeChannel merchantRechargeChannel = Optional.ofNullable(iMerchantRechargeChannelService.getById(form.getChannelId()))
                .orElseThrow(() -> new BizException(ResultCode.PARAM_ERROR));

        Merchant merchant = Optional.ofNullable(this.getById(merchantRechargeChannel.getMerchantId()))
                .orElseThrow(() -> new BizException(ResultCode.PARAM_ERROR));

        IThirdPartPayService iThirdPartPayService = thirdPartPaymentExecutor.findByMerchantCode(merchant.getCode());

        BasePayDTO payDTO = BasePayDTO.builder()
                .orderId(UUID.fastUUID().toString(true))
                .channelId(merchantRechargeChannel.getParam())
                .userIp(ServletUtil.getClientIP(request))
                .build();

        Boolean isSuccess = iThirdPartPayService.checkChannel(merchant, payDTO);

        // 測試通道失敗則關閉通道
        if (Boolean.FALSE.equals(isSuccess)) {
            iMerchantRechargeChannelService.lambdaUpdate()
                    .eq(MerchantRechargeChannel::getId, merchantRechargeChannel.getId())
                    .set(MerchantRechargeChannel::getEnable, 0)
                    .update();
        }

        return isSuccess;
    }

    @Override
    public List<OptionVO<Long>> findMerchantOption() {
        return this.list()
                .stream()
                .map(merchant -> OptionVO.<Long>builder()
                        .value(merchant.getId())
                        .label(merchant.getCode())
                        .build()
                )
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<CheckChannelVO> findCheckChannel(FindCheckChannelForm form) {
        List<Bank> banks = iBankService.list();

        List<RechargeType> rechargeTypes = iRechargeTypeService.list();

        List<MerchantRechargeChannel> merchantRechargeChannels = iMerchantRechargeChannelService.lambdaQuery()
                .eq(MerchantRechargeChannel::getMerchantId, form.getMerchantId())
                .eq(Objects.nonNull(form.getRechargeTypeId()), MerchantRechargeChannel::getRechargeTypeId, form.getRechargeTypeId())
                .eq(Objects.nonNull(form.getBankId()), MerchantRechargeChannel::getBankId, form.getBankId())
                .list();

        return merchantRechargeChannels.stream()
                .map(merchantRechargeChannel ->
                        {
                            RechargeType rechargeType = rechargeTypes.stream()
                                    .filter(filter -> Objects.equals(filter.getId(), merchantRechargeChannel.getRechargeTypeId()))
                                    .findFirst()
                                    .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

                            Bank bank = banks.stream()
                                    .filter(filter -> Objects.equals(filter.getId(), merchantRechargeChannel.getBankId()))
                                    .findFirst()
                                    .orElse(Bank.builder().build());

                            return CheckChannelVO.builder()
                                    .channelId(merchantRechargeChannel.getId())
                                    .rechargeTypeId(merchantRechargeChannel.getRechargeTypeId())
                                    .rechargeTypeName(rechargeType.getName())
                                    .bankId(merchantRechargeChannel.getBankId())
                                    .bankName(bank.getName())
                                    .build();
                        }
                )
                .collect(Collectors.toList());
    }

    public List<MerchantRechargeOptionVO> findMerchantAndRechargeOptions() {
        List<MerchantRechargeType> merchantRechargeTypes = iMerchantRechargeTypeService.list();

        List<MerchantRechargeOptionVO> optionVOS = new ArrayList<>(this.list()
                .stream()
                .map(merchant ->MerchantRechargeOptionVO.builder()
                        .value(merchant.getId())
                        .label(merchant.getName())
                        .children(merchantRechargeTypes.stream()
                                .filter(rechargeType -> rechargeType.getMerchantId().equals(merchant.getId()))
                                .map(rechargeType ->MerchantRechargeOptionVO.builder()
                                        .value(rechargeType.getRechargeTypeId())
                                        .label(rechargeType.getRechargeTypeName())
                                        .minAmount(rechargeType.getMinAmount())
                                        .maxAmount(rechargeType.getMaxAmount())
                                        .build()).collect(Collectors.toList()))
                        .build()
                )
                .collect(Collectors.toUnmodifiableList()));

        optionVOS.add(MerchantRechargeOptionVO.builder()
                .value(0L)
                .label("推薦卡轉卡")
                .children(iCompanyBankCardGroupService.lambdaQuery()
                        .select(CompanyBankCardGroup::getId, CompanyBankCardGroup::getName)
                        .orderByAsc(CompanyBankCardGroup::getId)
                        .list()
                        .stream()
                        .map(x -> MerchantRechargeOptionVO.builder().value(x.getId()).label(x.getName()).build())
                        .collect(Collectors.toList())).build());
        return optionVOS;
    }


    public List<OptionVO<Long>> findMerchantAndRechargeOption() {
        List<MerchantRechargeType> merchantRechargeTypes = iMerchantRechargeTypeService.list();


        List<OptionVO<Long>> optionVOS = new ArrayList<>(this.list()
                .stream()
                .map(merchant -> OptionVO.<Long>builder()
                        .value(merchant.getId())
                        .label(merchant.getName())
                        .children(merchantRechargeTypes.stream()
                                .filter(rechargeType -> rechargeType.getMerchantId().equals(merchant.getId()))
                                .map(rechargeType -> OptionVO.<Long>builder()
                                        .value(rechargeType.getRechargeTypeId())
                                        .label(rechargeType.getRechargeTypeName())
                                        .build()).collect(Collectors.toList()))
                        .build()
                )
                .collect(Collectors.toUnmodifiableList()));

        OptionVO<Long> optionVO = new OptionVO<>(0L, "推薦卡轉卡");
        optionVO.setChildren(iCompanyBankCardGroupService.lambdaQuery()
                .select(CompanyBankCardGroup::getId, CompanyBankCardGroup::getName)
                .orderByAsc(CompanyBankCardGroup::getId)
                .list()
                .stream()
                .map(x -> new OptionVO<>(x.getId(), x.getName()))
                .collect(Collectors.toList()));
        optionVOS.add(optionVO);

        return optionVOS;
    }

    @Override
    public List<OptionVO<Long>> findMerchantAndRechargeMerchantOption(Long merchantId) {
        return iMerchantRechargeTypeService.lambdaQuery()
                .select(MerchantRechargeType::getRechargeTypeId, MerchantRechargeType::getRechargeTypeName)
                .eq(MerchantRechargeType::getMerchantId, merchantId)
                .list()
                .stream()
                .map(rechargeType -> OptionVO.<Long>builder()
                        .value(rechargeType.getRechargeTypeId())
                        .label(rechargeType.getRechargeTypeName())
                        .build()
                )
                .collect(Collectors.toList());
    }
}





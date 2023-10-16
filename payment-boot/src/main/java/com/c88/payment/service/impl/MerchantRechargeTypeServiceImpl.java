package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.payment.constants.RedisKey;
import com.c88.payment.mapper.MerchantRechargeTypeMapper;
import com.c88.payment.mapstruct.MerchantRechargeTypeConverter;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.entity.MerchantRechargeChannel;
import com.c88.payment.pojo.entity.MerchantRechargeType;
import com.c88.payment.pojo.form.ModifyMerchantRechargeType;
import com.c88.payment.pojo.vo.MerchantRechargeTypeVO;
import com.c88.payment.service.IBankService;
import com.c88.payment.service.IMerchantRechargeChannelService;
import com.c88.payment.service.IMerchantRechargeTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantRechargeTypeServiceImpl extends ServiceImpl<MerchantRechargeTypeMapper, MerchantRechargeType> implements IMerchantRechargeTypeService {

    private final MerchantRechargeTypeConverter merchantRechargeTypeConverter;

    private final IBankService iBankService;

    private final IMerchantRechargeChannelService iMerchantRechargeChannelService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<MerchantRechargeTypeVO> findMerchantRechargeType(Integer merchantId) {
        List<Bank> banks = iBankService.list();

        List<MerchantRechargeChannel> merchantRechargeChannels = iMerchantRechargeChannelService.lambdaQuery().eq(MerchantRechargeChannel::getMerchantId, merchantId).list();

        List<MerchantRechargeTypeVO> merchantRechargeTypes = this.lambdaQuery()
                .eq(MerchantRechargeType::getMerchantId, merchantId)
                .list()
                .stream()
                .map(merchantRechargeTypeConverter::toVo)
                .collect(Collectors.toUnmodifiableList());

        merchantRechargeTypes.forEach(merchantRechargeType -> {

                    List<Long> bankIds = merchantRechargeChannels.stream()
                            .filter(filter -> Objects.equals(filter.getRechargeTypeId(), merchantRechargeType.getRechargeTypeId().intValue()))
                            .map(MerchantRechargeChannel::getBankId)
                            .collect(Collectors.toUnmodifiableList());

                    List<String> bankNames = banks.stream()
                            .filter(filter -> bankIds.contains(filter.getId()))
                            .map(Bank::getName)
                            .collect(Collectors.toUnmodifiableList());
                    merchantRechargeType.setSupportBanks(bankNames);
                }
        );

        return merchantRechargeTypes;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisKey.USAGE_RECHARGE_TYPE, allEntries = true)
    })
    public Boolean modifyMerchantRechargeType(ModifyMerchantRechargeType form) {
        Set<String> chooseChannelQueue = redisTemplate.keys("chooseChannelQueue*");
        redisTemplate.delete(chooseChannelQueue);
        Set<String> chooseChannelStrategy = redisTemplate.keys("chooseChannelStrategy*");
        redisTemplate.delete(chooseChannelStrategy);
        return this.lambdaUpdate()
                .eq(MerchantRechargeType::getId, form.getId())
                .set(Objects.nonNull(form.getEnable()), MerchantRechargeType::getEnable, form.getEnable())
                .set(Objects.nonNull(form.getNote()), MerchantRechargeType::getNote, form.getNote())
                .set(Objects.nonNull(form.getFeeRate()), MerchantRechargeType::getFeeRate, form.getFeeRate())
                .set(Objects.nonNull(form.getMinAmount()) && Objects.nonNull(form.getMaxAmount()), MerchantRechargeType::getMinAmount, form.getMinAmount())
                .set(Objects.nonNull(form.getMinAmount()) && Objects.nonNull(form.getMaxAmount()), MerchantRechargeType::getMaxAmount, form.getMaxAmount())
                .update();
    }

}





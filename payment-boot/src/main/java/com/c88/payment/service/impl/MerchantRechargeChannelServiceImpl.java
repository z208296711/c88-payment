package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.payment.mapper.MerchantRechargeChannelMapper;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.entity.MemberChannel;
import com.c88.payment.pojo.entity.MerchantRechargeChannel;
import com.c88.payment.pojo.form.ModifyMerchantRechargeChannelForm;
import com.c88.payment.service.IMerchantRechargeChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 *
 */
@Service
@RequiredArgsConstructor
public class MerchantRechargeChannelServiceImpl extends ServiceImpl<MerchantRechargeChannelMapper, MerchantRechargeChannel> implements IMerchantRechargeChannelService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Bank> findMatchRechargeChannelBanks(MemberChannel memberChannel) {
        return this.baseMapper.findMatchRechargeChannelBanks(memberChannel);
    }

    @Override
    public Boolean modifyMerchantRechargeChannel(ModifyMerchantRechargeChannelForm form) {
        Set<String> chooseChannelQueue = redisTemplate.keys("chooseChannelQueue*");
        redisTemplate.delete(chooseChannelQueue);
        Set<String> chooseChannelStrategy = redisTemplate.keys("chooseChannelStrategy*");
        redisTemplate.delete(chooseChannelStrategy);
        return this.lambdaUpdate()
                .eq(MerchantRechargeChannel::getId, form.getId())
                .set(MerchantRechargeChannel::getEnable, form.getEnable())
                .update();
    }
}





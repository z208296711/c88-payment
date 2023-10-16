package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.entity.MemberChannel;
import com.c88.payment.pojo.entity.MerchantRechargeChannel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.c88.payment.pojo.entity.MerchantRechargeChannel
 */
public interface MerchantRechargeChannelMapper extends BaseMapper<MerchantRechargeChannel> {

    List<Bank> findMatchRechargeChannelBanks(@Param("memberChannel") MemberChannel memberChannel);
}





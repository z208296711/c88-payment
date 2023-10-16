package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.entity.MemberChannel;
import com.c88.payment.pojo.entity.MerchantRechargeChannel;
import com.c88.payment.pojo.form.ModifyMerchantRechargeChannelForm;

import java.util.List;

/**
 *
 */
public interface IMerchantRechargeChannelService extends IService<MerchantRechargeChannel> {

    List<Bank> findMatchRechargeChannelBanks(MemberChannel memberChannel);

    Boolean modifyMerchantRechargeChannel(ModifyMerchantRechargeChannelForm form);
}

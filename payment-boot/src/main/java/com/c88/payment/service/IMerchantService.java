package com.c88.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.member.vo.OptionVO;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.form.CheckChannelForm;
import com.c88.payment.pojo.form.FindCheckChannelForm;
import com.c88.payment.pojo.form.FindMerchantPageForm;
import com.c88.payment.pojo.form.ModifyMerchantNoteForm;
import com.c88.payment.pojo.vo.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public interface IMerchantService extends IService<Merchant> {

    Merchant findByCode(String code);

    Merchant getMerchantById(Long id);

    IPage<MerchantVO> findMerchantPage(FindMerchantPageForm form);

    Boolean modifyMerchant(ModifyMerchantNoteForm form);

    MerchantStateSettingVO findMerchantState(Integer merchantId);

    List<MerchantSettingOptionVO> findMerchantSettingOption();

    Boolean checkChannel(CheckChannelForm channelId, HttpServletRequest request);

    List<OptionVO<Long>> findMerchantOption();

    List<CheckChannelVO> findCheckChannel(FindCheckChannelForm merchantId);

    List<MerchantRechargeOptionVO> findMerchantAndRechargeOptions();

    List<OptionVO<Long>> findMerchantAndRechargeMerchantOption(Long merchantId);
}

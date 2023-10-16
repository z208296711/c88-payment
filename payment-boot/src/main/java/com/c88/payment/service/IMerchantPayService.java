package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.entity.MerchantPay;
import com.c88.payment.pojo.form.FindMerchantPayForm;
import com.c88.payment.pojo.form.ModifyMerchantPayForm;
import com.c88.payment.pojo.vo.MerchantPayVO;

import java.util.Set;

/**
 * @author user
 * @description 针对表【payment_merchant_pay_setting(第三方廠商代付管理)】的数据库操作Service
 * @createDate 2022-08-22 10:38:17
 */
public interface IMerchantPayService extends IService<MerchantPay> {

    Page<MerchantPayVO> findMerchantPay(FindMerchantPayForm form);

    Boolean modifyMerchantPay(ModifyMerchantPayForm form);

    Set<Integer> findMerchantPayUseVipIds();
}

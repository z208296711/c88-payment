package com.c88.payment.service;

import com.c88.payment.pojo.entity.MerchantRechargeType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.form.ModifyMerchantRechargeType;
import com.c88.payment.pojo.vo.MerchantRechargeTypeVO;

import java.util.List;

public interface IMerchantRechargeTypeService extends IService<MerchantRechargeType> {

    List<MerchantRechargeTypeVO> findMerchantRechargeType(Integer merchantId);

    Boolean modifyMerchantRechargeType(ModifyMerchantRechargeType form);
}

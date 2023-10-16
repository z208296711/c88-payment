package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.entity.AffiliateBankCard;
import com.c88.payment.pojo.form.AffiliateBankCardForm;
import com.c88.payment.pojo.vo.AffiliateBankCardVO;

import java.util.List;

public interface IAffiliateBankCardService extends IService<AffiliateBankCard> {

    List<AffiliateBankCardVO> findAffiliateBankCard(Long affiliateId);

    Boolean addAffiliateBankCard(Long affiliateId, AffiliateBankCardForm form);

    Boolean deleteBankCard(Long id);

}

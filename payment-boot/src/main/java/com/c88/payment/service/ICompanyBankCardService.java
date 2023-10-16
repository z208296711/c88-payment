package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.member.vo.OptionVO;
import com.c88.payment.pojo.entity.CompanyBankCard;
import com.c88.payment.pojo.vo.CompanyBankCardVO;

import java.util.List;

/**
 *
 */
public interface ICompanyBankCardService extends IService<CompanyBankCard> {

    Page<CompanyBankCardVO> findCompanyCardByGroupId(Integer groupId, Page<CompanyBankCardVO> page);

    List<CompanyBankCard> findCompanyCardCanUsedByGroupId(List<Long> groupId);

    List<OptionVO<Long>> getAllCompanyBankCard();

}

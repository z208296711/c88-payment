package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.entity.CompanyBankCardGroup;
import com.c88.payment.pojo.vo.CompanyBankCardGroupVO;

/**
 *
 */
public interface ICompanyBankCardGroupService extends IService<CompanyBankCardGroup> {

    Page<CompanyBankCardGroupVO> findCompanyCardGroup(String name, Page<CompanyBankCardGroupVO> page);
}

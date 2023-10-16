package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.payment.mapper.CompanyBankCardGroupMapper;
import com.c88.payment.pojo.entity.CompanyBankCardGroup;
import com.c88.payment.pojo.vo.CompanyBankCardGroupVO;
import com.c88.payment.service.ICompanyBankCardGroupService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class CompanyBankCardGroupServiceImpl extends ServiceImpl<CompanyBankCardGroupMapper, CompanyBankCardGroup>
    implements ICompanyBankCardGroupService {

    @Override
    public Page<CompanyBankCardGroupVO> findCompanyCardGroup(String name , Page<CompanyBankCardGroupVO> page) {
        return this.baseMapper.findCompanyCardGroup(name, page);
    }
}





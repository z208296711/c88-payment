package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.payment.pojo.entity.CompanyBankCardGroup;
import com.c88.payment.pojo.vo.CompanyBankCardGroupVO;

public interface CompanyBankCardGroupMapper extends BaseMapper<CompanyBankCardGroup> {

    Page<CompanyBankCardGroupVO> findCompanyCardGroup(String name, Page<CompanyBankCardGroupVO> page);

}





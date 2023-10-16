package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.payment.pojo.entity.CompanyBankCard;
import com.c88.payment.pojo.vo.CompanyBankCardVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.c88.payment.pojo.entity.CompanyBankCard
 */
public interface CompanyBankCardMapper extends BaseMapper<CompanyBankCard> {

    Page<CompanyBankCardVO> findCompanyCardPage(Page<CompanyBankCardVO> page, @Param("ew") QueryWrapper<CompanyBankCard> queryWrapper);

    List<CompanyBankCard> findCompanyCardCanUsedByGroupId(@Param("groupIds") List<Long> groupIds);
}





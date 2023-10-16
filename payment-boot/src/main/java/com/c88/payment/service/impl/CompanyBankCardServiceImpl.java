package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.member.vo.OptionVO;
import com.c88.payment.enums.BankCardStatusEnum;
import com.c88.payment.mapper.CompanyBankCardMapper;
import com.c88.payment.mapstruct.CompanyBankCardConverter;
import com.c88.payment.pojo.entity.CompanyBankCard;
import com.c88.payment.pojo.vo.CompanyBankCardVO;
import com.c88.payment.service.ICompanyBankCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
@RequiredArgsConstructor
public class CompanyBankCardServiceImpl extends ServiceImpl<CompanyBankCardMapper, CompanyBankCard>  implements ICompanyBankCardService {

    private final CompanyBankCardConverter companyBankCardConverter;

    @Override
    public Page<CompanyBankCardVO> findCompanyCardByGroupId(Integer groupId, Page<CompanyBankCardVO> page) {
        QueryWrapper<CompanyBankCard> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq(groupId != null, "group_id", groupId)
                .eq("pc.deleted", BankCardStatusEnum.DISABLE)
                .orderByDesc("id");
        return this.baseMapper.findCompanyCardPage(page, queryWrapper);
    }

    @Override
    public List<CompanyBankCard> findCompanyCardCanUsedByGroupId(List<Long> groupIds) {
        return this.baseMapper.findCompanyCardCanUsedByGroupId(groupIds);
    }

    @Override
    public List<OptionVO<Long>> getAllCompanyBankCard() {
        return this.lambdaQuery()
                .eq(CompanyBankCard::getEnable, BankCardStatusEnum.ENABLE.getValue())
                .select(CompanyBankCard::getId,CompanyBankCard::getCode)
                .list()
                .stream()
                .map(x -> new OptionVO<>(x.getId(), x.getCode()))
                .collect(Collectors.toList());
    }

}





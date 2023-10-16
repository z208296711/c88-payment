package com.c88.payment.mapper;

import com.c88.payment.pojo.entity.Bank;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.c88.payment.pojo.entity.Bank
 */
public interface BankMapper extends BaseMapper<Bank> {
    boolean modifyBankSortBottom(Long id);

    boolean modifyBankSortTop(Long id);
}





package com.c88.payment.mapstruct;

import com.c88.payment.dto.BankDTO;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.form.BankSortForm;
import com.c88.payment.pojo.form.MaintainBankForm;
import com.c88.payment.pojo.vo.BankVO;
import com.c88.payment.pojo.vo.H5BankVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BankConverter   {

    BankDTO toDTO(Bank entity);

    BankVO toVo(Bank entity);

    Bank toEntity(BankVO vo);

    Bank toEntity(MaintainBankForm form);

    Bank toEntity(BankSortForm form);

    MaintainBankForm toMaintainBankForm(Bank bank);

    H5BankVO toH5BankVO(Bank entity);

    List<H5BankVO> toH5BankVO(List<Bank> entity);



}

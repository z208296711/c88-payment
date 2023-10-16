package com.c88.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.member.vo.OptionVO;
import com.c88.payment.dto.BankDTO;
import com.c88.payment.enums.BankStateEnum;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.form.*;
import com.c88.payment.pojo.vo.AdminBankVO;
import com.c88.payment.pojo.vo.BankVO;

import java.util.List;

/**
 * @author user
 * @description 针对表【payment_bank(銀行)】的数据库操作Service
 * @createDate 2022-05-26 11:43:15
 */
public interface IBankService extends IService<Bank> {

    void bankMaintainTimeProcess(MaintainBankForm form);

    List<BankVO> findBank();

    List<BankDTO> findBankDTO();

    IPage<AdminBankVO> findAdminBankPage(AdminSearchBankForm form);

    Boolean addBank(AddBankForm form);

    Boolean modifyBankMaintainTime(MaintainBankForm form);

    Boolean modifyBank(ModifyBankForm form);

    Boolean modifyBank(Bank bank);

    Boolean modifyBank(List<Bank> bank);

    Boolean modifyBankSortTopBottom(ModifyBankSortTopBottomForm form);

    BankStateEnum getBankState(Bank bank);

    Boolean modifyBankSort(ModifyBankSortForm form);

    List<OptionVO<Long>> findBankOption();

    List<OptionVO<Long>> findBankOptionByMerchantIdOption(Long merchantId);
}

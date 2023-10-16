package com.c88.payment.mapstruct;

import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.form.AddBankForm;
import com.c88.payment.pojo.form.ModifyBankForm;
import com.c88.payment.pojo.vo.AdminBankVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminBankConverter  {

    Bank fromToEntity(AddBankForm bankForm);

    Bank fromToEntity(ModifyBankForm bankForm);

    AdminBankVO entityToAdminBankVO(Bank bank);

}

package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.form.BankSortForm;
import com.c88.payment.pojo.form.MaintainBankForm;
import com.c88.payment.pojo.vo.BankVO;
import com.c88.payment.pojo.vo.OnlineMemberRechargeVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OnlineMemberRechargeConverter extends BaseConverter<MemberRecharge, OnlineMemberRechargeVO> {

}

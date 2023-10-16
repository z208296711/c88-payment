package com.c88.payment.mapstruct;

import com.c88.payment.dto.MemberBalanceInfoDTO;
import com.c88.payment.dto.PaymentMemberBalanceDTO;
import com.c88.payment.pojo.entity.MemberBalance;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberBalanceConverter {

    PaymentMemberBalanceDTO toDTO(MemberBalance entity);

    List<PaymentMemberBalanceDTO> toDTO(List<MemberBalance> entity);

    MemberBalanceInfoDTO toMemberInfoDTO(MemberBalance entity);

}

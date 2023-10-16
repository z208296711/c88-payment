package com.c88.payment.mapstruct;

import com.c88.payment.dto.MemberRechargeSuccessDTO;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.entity.RechargeType;
import com.c88.payment.pojo.vo.H5MemberAccountRechargeRecordVO;
import com.c88.payment.vo.MemberDepositDTO;
import com.c88.payment.vo.MemberRechargeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberRechargeConverter {

    MemberRechargeDTO toDTO(MemberRecharge entity);

    MemberRechargeSuccessDTO toMemberRechargeSuccessDTO(MemberRecharge entity);

    List<MemberRechargeDTO> toDTO(List<MemberRecharge> entity);

    default List<MemberDepositDTO> toDepositDTOs(List<MemberRecharge> entityX, Map<String, RechargeType> rechargeTypesMap) {
        return entityX.stream().map(e -> toDepositDTO(e, rechargeTypesMap)).collect(Collectors.toList());
    }

    default MemberDepositDTO toDepositDTO(MemberRecharge entity, Map<String, RechargeType> rechargeTypesMap) {
        MemberDepositDTO dto = new MemberDepositDTO();
        dto.setAmount(entity.getAmount());
        dto.setRechargeAwardAmount(entity.getRechargeAwardAmount());
        dto.setRealAmount(entity.getRealAmount());
        dto.setCreateTime(entity.getCreateTime());
        dto.setStatus(entity.getStatus());
        dto.setRechargeAwardAmount(entity.getRechargeAwardAmount());
        dto.setTradeNo(entity.getTradeNo());
        dto.setUsername(entity.getUsername());
        dto.setRechargeType(Optional.ofNullable(rechargeTypesMap.get(entity.getRechargeTypeId().toString()))
                .map(r -> r.getName()).orElse(""));
        return dto;
    }

    @Mapping(target = "rechargeTypeI18N", expression = "java(map.getOrDefault(entity.getRechargeTypeId(),\"\"))")
    H5MemberAccountRechargeRecordVO toRechargeVO(MemberRecharge entity, Map<Integer, String> map);

}

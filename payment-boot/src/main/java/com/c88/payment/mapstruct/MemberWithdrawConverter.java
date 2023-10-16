package com.c88.payment.mapstruct;

import com.c88.common.core.enums.RemitStateEnum;
import com.c88.payment.enums.MemberAccountWithdrawRecordEnum;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.vo.H5MemberAccountWithdrawRecordVO;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Objects;

import static com.c88.common.core.enums.WithdrawStateEnum.APPLY;
import static com.c88.common.core.enums.WithdrawStateEnum.APPROVED;
import static com.c88.common.core.enums.WithdrawStateEnum.REJECTED;
import static com.c88.common.core.enums.WithdrawStateEnum.SECOND;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberWithdrawConverter {

    H5MemberAccountWithdrawRecordVO toVO(MemberWithdraw entity);

    @BeforeMapping
    default void memberWithdrawStatus(@MappingTarget H5MemberAccountWithdrawRecordVO vo, MemberWithdraw entity) {
        Byte state = entity.getState();
        Byte remitState = entity.getRemitState();
        if (Objects.equals(state, APPLY.getState()) || Objects.equals(state, SECOND.getState())) {
            // 審核中
            vo.setStatus(MemberAccountWithdrawRecordEnum.REVIEW.getCode());
        } else if (Objects.equals(state, REJECTED.getState())) {
            // 審核拒絕
            vo.setStatus(MemberAccountWithdrawRecordEnum.REJECTED.getCode());
        } else if (Objects.equals(state, APPROVED.getState())) {
            // 審核通過
            if (List.of(RemitStateEnum.APPLY.getState(), RemitStateEnum.PENDING.getState(), RemitStateEnum.PAY_PENDING.getState()).contains(remitState)) {
                // 出款中
                vo.setStatus(MemberAccountWithdrawRecordEnum.WITHDRAWING.getCode());
            } else if (List.of(RemitStateEnum.SUCCESS.getState(), RemitStateEnum.PAY_SUCCESS.getState()).contains(remitState)) {
                // 出款成功
                vo.setStatus(MemberAccountWithdrawRecordEnum.SUCCESS.getCode());
            } else {
                // 出款失敗
                vo.setStatus(MemberAccountWithdrawRecordEnum.FAIL.getCode());
            }
        }
    }

}

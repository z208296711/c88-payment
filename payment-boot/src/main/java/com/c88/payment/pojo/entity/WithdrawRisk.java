package com.c88.payment.pojo.entity;

import com.c88.common.core.enums.RiskTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WithdrawRisk {

    private Integer withdrawId;
    private Byte riskType;

    public WithdrawRisk(Integer withdrawId, RiskTypeEnum riskTypeEnum) {
        this.withdrawId = withdrawId;
        this.riskType = riskTypeEnum.getValue();
    }

}

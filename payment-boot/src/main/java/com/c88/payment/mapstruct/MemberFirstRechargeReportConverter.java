package com.c88.payment.mapstruct;

import com.c88.payment.pojo.entity.MemberFirstRechargeReport;
import com.c88.payment.pojo.vo.MemberFirstRechargeReportVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberFirstRechargeReportConverter {

    MemberFirstRechargeReportVO toVO(MemberFirstRechargeReport entity);

}

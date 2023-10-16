package com.c88.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.dto.MemberBalanceInfoDTO;
import com.c88.payment.dto.PaymentMemberBalanceDTO;
import com.c88.payment.pojo.entity.MemberBalance;
import com.c88.payment.pojo.form.RechargeWithdrawReportForm;
import com.c88.payment.pojo.vo.CompanyReportVO;
import com.c88.payment.pojo.vo.PageSummaryVO;
import com.c88.payment.pojo.vo.RechargeWithdrawAwardCountVO;
import com.c88.payment.pojo.vo.RechargeWithdrawReportVO;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 */
public interface IMemberBalanceService extends IService<MemberBalance> {

    MemberBalance findByMemberId(Long memberId);

    MemberBalance findByUsername(String username);

    PaymentMemberBalanceDTO findByMemberIdDTO(Long memberId);

    PaymentMemberBalanceDTO findByUsernameDTO(String username);

    BigDecimal addBalance(AddBalanceDTO addBalanceDTO);

    List<PaymentMemberBalanceDTO> findByMemberIdArrayDTO(List<Long> memberIds);

    List<MemberBalanceInfoDTO> findMemberBalanceInfoByIds(List<Long> memberIds);

    List<MemberBalanceInfoDTO> findMemberBalanceInfoByUsernames(List<String> usernames);

    PageSummaryVO getRechargeWithdrawReport(RechargeWithdrawReportForm form);

    CompanyReportVO getFirstRechargeCountAndAmount(String startTime, String endTime);

    RechargeWithdrawAwardCountVO getRechargeAwardCountAndAmount(String startTime, String endTime);

    RechargeWithdrawAwardCountVO getWithdrawCountAndAmount(String startTime, String endTime);
}

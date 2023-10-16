package com.c88.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.member.vo.MemberRechargeAwardTemplateClientVO;
import com.c88.payment.form.SearchMemberDepositForm;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.entity.MemberChannel;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.form.*;
import com.c88.payment.pojo.vo.*;
import com.c88.payment.vo.MemberDepositDTO;
import com.c88.payment.vo.MemberTotalRechargeVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IMemberRechargeService extends IService<MemberRecharge> {

    IPage<OnlineMemberRechargeVO> findOnlineRecharge(FindOnlineRechargeForm query);

    IPage<RechargeVO> findInlineRecharge(FindInlineRechargeForm form);

    List<RechargeTypeVO> findMemberRecharge(MemberInfoDTO memberInfoDTO);

    AddOnlineMemberRechargeVO addOnlineMemberRecharge(Long memberId, AddOnlineMemberRechargeForm form, HttpServletRequest request);

    MemberRechargeFormVO inlineRecharge(Long memberId, MemberRechargeForm form, HttpServletRequest request);

    boolean updateRecharge(RechargeModifyForm form);

    /**
     * 取得會員總充值金額
     *
     * @param uid      會員id
     * @param fromTime 從此時間後
     * @return
     */
    BigDecimal memberTotalRecharge(Long uid, LocalDateTime fromTime);

    BigDecimal memberTotalRecharge(Long uid, LocalDateTime fromTime, LocalDateTime toTime);

    List<RechargeInlineExcelVO> findInlineRechargeForExcel(FindInlineRechargeForm form);

    MemberRechargeInfo findMemberRechargeInfo(MemberChannel channel, List<Bank> banks);

    Boolean onlineMakeUp(OnlineMakeUpForm form, HttpServletRequest request);

    Boolean checkInlineRecharge(Long form, String remark);

    Boolean inlineMakeUp(InlineMakeUpForm form);

    void exportOnlineMemberRecharge(ExportOnlineMemberRechargeForm form, HttpServletResponse response);

    H5RechargeVO findMemberRecharges(Long memberId);

    List<MemberTotalRechargeVO> findMemberTotalRecharge(LocalDateTime startTime, LocalDateTime endTime);

    MemberRechargeAwardTemplateClientVO getMemberRechargeAwardTemplateClientVO(Long id,BigDecimal rechargeAmount);

    BigDecimal rechargeAwardAmount(MemberRechargeAwardTemplateClientVO template, BigDecimal amount);

    IPage<MemberDepositDTO> findMemberRechargeFromAffiliate(SearchMemberDepositForm form);

    List<RechargeWithdrawReportVO> getSuccessRechargeCount(Integer status,List<Integer> types, String startTime, String endTime);

    List<Long> getRechargeMembers(String startTime, String endTime);

    BigDecimal getFee(String startTime, String endTime);
}

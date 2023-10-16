package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.form.FindInlineRechargeForm;
import com.c88.payment.pojo.form.SearchMemberWinLossForm;
import com.c88.payment.pojo.vo.*;
import com.c88.payment.vo.MemberTotalRechargeVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberRechargeMapper extends BaseMapper<MemberRecharge> {

    Page<RechargeVO> queryOnlineRechargeList(Page<MemberRecharge> page, @Param("queryParams") FindInlineRechargeForm form);

    Page<RechargeVO> queryInlineRechargeList(Page<MemberRecharge> page, @Param("queryParams") FindInlineRechargeForm form);

    List<RechargeInlineExcelVO> queryInlineRechargeListFormExcel(@Param("queryParams") FindInlineRechargeForm form);

    List<RechargeTypeVO> findMemberRechargeTypes(Long memberId);

    List<MemberTotalRechargeVO> findMemberTotalRecharge(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<MemberWinLossVO> findMemberWinLossByDate(SearchMemberWinLossForm form);

    @Select("SELECT username as memberUsername, count(member_id) AS rechargeCount ,ifnull(sum(amount),0) as rechargeTotalAmount, ifnull(sum(fee),0) as rechargeFee from payment_member_recharge " +
            " ${ew.customSqlSegment}")
    List<RechargeWithdrawReportVO> getSuccessRechargeCount(@Param("status") Integer status,@Param("types") List<Integer> types, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param(Constants.WRAPPER) QueryWrapper<RechargeWithdrawReportVO> queryWrapper);
}

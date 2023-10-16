package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.FindRemitForm;
import com.c88.payment.pojo.form.FindWithdrawForm;
import com.c88.payment.pojo.vo.RechargeWithdrawAwardCountVO;
import com.c88.payment.pojo.vo.RechargeWithdrawReportVO;
import com.c88.payment.pojo.vo.withdraw.RemitReportVO;
import com.c88.payment.pojo.vo.withdraw.RemitVO;
import com.c88.payment.pojo.vo.withdraw.WithdrawReportVO;
import com.c88.payment.pojo.vo.withdraw.WithdrawVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WithdrawMapper extends BaseMapper<MemberWithdraw> {
    Page<WithdrawVO> queryWithdraw(Page<WithdrawVO> page, @Param("queryParams") FindWithdrawForm findWithdrawForm);

    List<WithdrawReportVO> listWithdraw(@Param("queryParams") FindWithdrawForm findWithdrawForm);

    Page<RemitVO> queryRemit(Page<WithdrawVO> page, @Param("queryParams") FindRemitForm findRemitForm);

    List<RemitReportVO> listRemit(@Param("queryParams") FindRemitForm findRemitForm);

    @Select("SELECT username as memberUsername,count(*) as withdrawCount, ifnull(sum(amount),0) as withdrawTotalAmount from payment_member_withdraw " +
            "where (remit_state=#{status1} or remit_state=#{status2}) and remit_time>#{startTime} and remit_time<#{endTime} group by username")
    List<RechargeWithdrawReportVO> getSuccessWithdrawCount(@Param("status1") Integer status1, @Param("status2") Integer status2, @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("select ifnull(count(*),0) as withdrawCount, ifnull(sum(amount),0) as withdrawAmount from payment_member_withdraw where (remit_state=#{status1} or remit_state=#{status2}) " +
            " and  remit_time>#{startTime}  and remit_time<#{endTime}")
    RechargeWithdrawAwardCountVO getSuccessWithdraw(@Param("status1") Integer status1, @Param("status2") Integer status2, @Param("startTime") String startTime, @Param("endTime") String endTime);
}

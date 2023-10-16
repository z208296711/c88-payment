package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.payment.pojo.entity.MemberBalance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.payment.pojo.vo.CompanyReportVO;
import com.c88.payment.pojo.vo.RechargeWithdrawAwardCountVO;
import com.c88.payment.pojo.vo.RechargeWithdrawReportVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author user
* @description 针对表【payment_member_balance】的数据库操作Mapper
* @createDate 2022-11-03 15:16:36
* @Entity com.c88.payment.pojo.entity.PaymentMemberBalance
*/
public interface PaymentMemberBalanceMapper extends BaseMapper<MemberBalance> {

    @Select("SELECT max(record.gmt_modified) as modifyTime , member.user_name as memberUsername,member.real_name as memberRealName" +
            ", member.login_count as loginCount,member.last_login_time as lastLoginTime,pmb.balance AS balance, member.gmt_create as registerTime," +
            " member.id as memberId " +
            "from payment_balance_change_record record " +
            "join member on record.member_id=member.id " +
            "left join payment_member_balance pmb on pmb.member_id=member.id " +
            "${ew.customSqlSegment}")
    List<RechargeWithdrawReportVO> getBasicInfo(@Param(Constants.WRAPPER) QueryWrapper<RechargeWithdrawReportVO> queryWrapper);

    @Select("SELECT " +
            " count( distinct( CASE WHEN type = 1 THEN member_id END )) AS rechargeCount, " +
            " ifnull(sum( CASE WHEN type = 1 THEN amount ELSE 0 END ),0) AS rechargeAmount, " +
//            " count( distinct(CASE WHEN type = 2 THEN member_id END )) AS withdrawCount, " +
//            " sum( CASE WHEN type = 2 THEN amount ELSE 0 END ) AS withdrawAmount, " +
            " count(distinct( CASE WHEN type = 3 THEN member_id END )) AS awardCount, " +
            " ifnull(sum( CASE WHEN type = 3 THEN amount ELSE 0 END ),0) AS awardAmount, " +
            " count(distinct( CASE WHEN type = 8 THEN member_id END )) AS rechargeAwardCount, " +
            " ifnull(sum( CASE WHEN type = 8 THEN amount ELSE 0 END ),0) AS rechargeAwardAmount  " +
            "from payment_balance_change_record where gmt_modified>#{startTime} and gmt_modified<#{endTime}")
    RechargeWithdrawAwardCountVO getRechargeWithdrawAwardCountAndAmount(@Param("startTime")String startTime, @Param("endTime")String endTime);
}





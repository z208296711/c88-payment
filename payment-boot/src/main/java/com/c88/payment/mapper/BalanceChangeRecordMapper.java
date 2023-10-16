package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.payment.pojo.entity.BalanceChangeRecord;
import com.c88.payment.pojo.vo.DailyRechargeWithdrawVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.Map;

/**
 * @author gary
 * @description 针对表【payment_balance_change_record(資金流動紀錄)】的数据库操作Mapper
 */
public interface BalanceChangeRecordMapper extends BaseMapper<BalanceChangeRecord> {

    @Select("select date(gmt_create) date," +
            "       IFNULL(SUM(CASE WHEN type = 1 THEN amount ELSE 0 END), 0) AS recharge," +
            "       IFNULL(SUM(CASE WHEN type = 2 THEN amount ELSE 0 END), 0) AS withdraw," +
            "       IFNULL(SUM(CASE WHEN type = 8 THEN amount ELSE 0 END), 0) AS rechargePromotion," +
            "       IFNULL(SUM(CASE WHEN type = 3 THEN amount ELSE 0 END), 0) AS bonus " +
            " from payment_balance_change_record" +
            " where " +
            " member_id = #{memberId} " +
            " and gmt_create >= #{startTime}" +
            " and gmt_create <= #{endTime}" +
            " group by date(convert_tz(gmt_create, '+00:00', #{zone}));")
    @MapKey("date")
    Map<LocalDate, DailyRechargeWithdrawVO> findDailyRechargeWithdrawReport(Long memberId,
                                                                            String zone,
                                                                            String startTime,
                                                                            String endTime);
}





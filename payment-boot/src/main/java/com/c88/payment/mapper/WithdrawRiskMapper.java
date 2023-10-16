package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.payment.pojo.entity.WithdrawRisk;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WithdrawRiskMapper extends BaseMapper<WithdrawRisk> {
    @Insert("INSERT IGNORE INTO withdraw_risk ( withdraw_id, risk_type ) VALUES (#{withdrawId},#{riskType})")
    boolean insertIgnore(WithdrawRisk withdrawRisk);
}

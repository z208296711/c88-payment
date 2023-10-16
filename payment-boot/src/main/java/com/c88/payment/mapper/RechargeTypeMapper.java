package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.payment.pojo.entity.RechargeType;
import com.c88.payment.pojo.vo.RechargeTypeVO;

import java.util.List;

/**
 * @Entity com.c88.payment.pojo.entity.RechargeType
 */
public interface RechargeTypeMapper extends BaseMapper<RechargeType> {

    List<RechargeTypeVO> findRechargeTypeOptionByVip(Integer vipId);
}





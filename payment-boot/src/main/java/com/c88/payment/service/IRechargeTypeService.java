package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.member.vo.OptionVO;
import com.c88.payment.pojo.entity.RechargeType;

import java.util.List;
import java.util.Map;

public interface IRechargeTypeService extends IService<RechargeType> {

    List<OptionVO<Integer>> findRechargeTypeOption();

    List<OptionVO<Integer>> findRechargeTypeOptionByVip(Integer vipId);
    Map<String , RechargeType> findRechargeTypeMap();

}

package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.member.vo.OptionVO;
import com.c88.common.core.enums.EnableEnum;
import com.c88.payment.mapper.RechargeTypeMapper;
import com.c88.payment.pojo.entity.RechargeType;
import com.c88.payment.service.IRechargeTypeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class RechargeTypeServiceImpl extends ServiceImpl<RechargeTypeMapper, RechargeType> implements IRechargeTypeService {

    @Override
    public List<OptionVO<Integer>> findRechargeTypeOption() {
        return this.list()
                .stream()
                .map(rechargeType -> OptionVO.<Integer>builder()
                        .value(rechargeType.getId())
                        .label(rechargeType.getName())
                        .build()
                )
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<OptionVO<Integer>> findRechargeTypeOptionByVip(Integer vipId) {
        return this.baseMapper.findRechargeTypeOptionByVip(vipId)
                .stream()
                .map(rechargeType -> OptionVO.<Integer>builder()
                        .value(rechargeType.getId())
                        .label(rechargeType.getName())
                        .build()
                )
                .collect(Collectors.toUnmodifiableList());
    }

    @Cacheable(cacheNames = "payment", key = "'rechargeTypes'")
    public Map<String, RechargeType> findRechargeTypeMap() {
        return this
                .lambdaQuery()
                .eq(RechargeType::getEnable, EnableEnum.START.getCode())
                .list()
                .stream()
                .collect(Collectors.toMap(x->x.getId().toString(), Function.identity()));
    }
}

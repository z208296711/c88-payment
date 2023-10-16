package com.c88.payment.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.payment.enums.BalanceChangeTypeEnum;
import com.c88.payment.mapper.BalanceChangeRecordMapper;
import com.c88.payment.pojo.entity.BalanceChangeRecord;
import com.c88.payment.pojo.form.AdminSearchMemberBalanceChangeForm;
import com.c88.payment.pojo.form.SearchRechargeWithdrawForm;
import com.c88.payment.pojo.vo.DailyRechargeWithdrawVO;
import com.c88.payment.service.IBalanceChangeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gary
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceChangeRecordServiceImpl extends ServiceImpl<BalanceChangeRecordMapper, BalanceChangeRecord> implements IBalanceChangeRecordService {


    @Override
    public IPage<DailyRechargeWithdrawVO> findDailyRechargeWithdrawReport(Long memberId, SearchRechargeWithdrawForm form) {
        Map<LocalDate, DailyRechargeWithdrawVO> dailyRechargeWithdrawVOMap = this.getBaseMapper().findDailyRechargeWithdrawReport(memberId, form.getZone(), form.getStartTime(), form.getEndTime());
        List<DailyRechargeWithdrawVO> dailyRechargeWithdrawVOList = new ArrayList<>();
        LocalDate startTime = LocalDate.parse(form.getStartTime().split(" ")[0]).plusDays(1);
        LocalDate endTime = LocalDate.parse(form.getEndTime().split(" ")[0]);
        startTime.datesUntil(endTime.plusDays(1)).forEach(date -> {
            dailyRechargeWithdrawVOList.add(dailyRechargeWithdrawVOMap.getOrDefault(date, new DailyRechargeWithdrawVO(date)));
        });
        IPage page = new Page<>();
        page.setRecords(dailyRechargeWithdrawVOList.stream()
                .sorted(Comparator.comparing(DailyRechargeWithdrawVO::getDate).reversed())
                .skip((long) (form.getPageNum() - 1) * form.getPageSize())
                .limit(form.getPageSize())
                .collect(Collectors.toList()));
        page.setCurrent(form.getPageNum());
        page.setPages(dailyRechargeWithdrawVOList.size() / form.getPageSize());
        page.setSize(form.getPageSize());
        page.setTotal(dailyRechargeWithdrawVOList.size());
        return page;
    }

    @Override
    public BigDecimal findBalanceChangeTotal(AdminSearchMemberBalanceChangeForm form) {
        BalanceChangeRecord balanceChangeRecord = this.getBaseMapper().selectOne(new QueryWrapper<BalanceChangeRecord>()
                .select("sum(amount) as amount")
                .lambda()
                .eq(BalanceChangeRecord::getMemberId, form.getMemberId())
                .le(StringUtils.isNotBlank(form.getStartTime()), BalanceChangeRecord::getGmtCreate, form.getStartTime())
                .ge(StringUtils.isNotBlank(form.getStartTime()), BalanceChangeRecord::getGmtCreate, form.getEndTime())
                .in(CollectionUtil.isNotEmpty(form.getTypes()), BalanceChangeRecord::getType, form.getTypes()));
        return balanceChangeRecord != null ? balanceChangeRecord.getAmount() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getSumForRiskAbnormal(Long memberId) {
        List<Integer> list = new ArrayList<>();
        list.add(BalanceChangeTypeEnum.RECHARGE.getValue());
        list.add(BalanceChangeTypeEnum.BONUS.getValue());
        list.add(BalanceChangeTypeEnum.RECHARGE_PROMOTION.getValue());
        BalanceChangeRecord balanceChangeRecord = this.getBaseMapper().selectOne(new QueryWrapper<BalanceChangeRecord>()
                .select("sum(amount) as amount")
                .lambda()
                .eq(BalanceChangeRecord::getMemberId, memberId)
                .in(BalanceChangeRecord::getType, list));

        return balanceChangeRecord.getAmount();
    }

}





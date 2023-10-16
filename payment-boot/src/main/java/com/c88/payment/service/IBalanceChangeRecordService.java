package com.c88.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.entity.BalanceChangeRecord;
import com.c88.payment.pojo.form.AddBalanceChangeForm;
import com.c88.payment.pojo.form.AdminSearchMemberBalanceChangeForm;
import com.c88.payment.pojo.form.SearchRechargeWithdrawForm;
import com.c88.payment.pojo.vo.DailyRechargeWithdrawVO;

import java.math.BigDecimal;

/**
 * @author gary
 * @description 针对表【payment_balance_change_record(資金流動紀錄)】的数据库操作Service
 * @createDate 2022-10-27 11:58:12
 */
public interface IBalanceChangeRecordService extends IService<BalanceChangeRecord> {

    IPage<DailyRechargeWithdrawVO> findDailyRechargeWithdrawReport(Long memberId, SearchRechargeWithdrawForm form);

    BigDecimal findBalanceChangeTotal(AdminSearchMemberBalanceChangeForm form);

    BigDecimal getSumForRiskAbnormal(Long memberId);

    // Boolean addAdjustBalanceChangeRecord(AddBalanceChangeForm form);
}

package com.c88.payment.service;

import com.c88.payment.pojo.entity.MemberWithdrawBet;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.form.AddBalanceChangeForm;

/**
* @author gary
* @description 针对表【payment_member_withdraw_bet】的数据库操作Service
* @createDate 2022-10-28 09:41:28
*/
public interface IMemberWithdrawBetService extends IService<MemberWithdrawBet> {


    MemberWithdrawBet findMemberWithdrawBet(Long memberId, String username);

}

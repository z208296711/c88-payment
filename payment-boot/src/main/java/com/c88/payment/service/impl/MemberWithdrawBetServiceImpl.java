package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.payment.mapper.MemberWithdrawBetMapper;
import com.c88.payment.pojo.entity.MemberWithdrawBet;
import com.c88.payment.service.IMemberWithdrawBetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author gary
 * @description 针对表【payment_member_withdraw_bet】的数据库操作Service实现
 * @createDate 2022-10-28 09:41:28
 */
@Service
@RequiredArgsConstructor
public class MemberWithdrawBetServiceImpl extends ServiceImpl<MemberWithdrawBetMapper, MemberWithdrawBet> implements IMemberWithdrawBetService {


    @Override
    public MemberWithdrawBet findMemberWithdrawBet(Long memberId, String username) {
        return this.lambdaQuery()
                .eq(MemberWithdrawBet::getMemberId, memberId)
                .oneOpt()
                .orElseGet(() -> {
                    MemberWithdrawBet memberWithdrawBet = MemberWithdrawBet.builder()
                            .memberId(memberId)
                            .username(username)
                            .accBet(BigDecimal.ZERO)
                            .validBet(BigDecimal.ZERO)
                            .build();
                    this.save(memberWithdrawBet);
                    return memberWithdrawBet;
                });
    }

}





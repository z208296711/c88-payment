package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.entity.MemberBank;
import com.c88.payment.pojo.form.MemberBankAddForm;
import com.c88.payment.pojo.form.MemberBankModifyForm;
import com.c88.payment.pojo.vo.MemberBankVO;

import java.util.List;

/**
 * @author user
 * @description 针对表【payment_member_bank(會員銀行)】的数据库操作Service
 * @createDate 2022-05-26 11:43:15
 */
public interface IMemberBankService extends IService<MemberBank> {

    List<MemberBankVO> findMemberBank(Long memberId);

    Boolean addMemberBank(Long memberId, MemberBankAddForm form);

    Boolean modifyMemberBank(Long memberId, MemberBankModifyForm form);

    Boolean deleteMemberBank(Integer id);

    Boolean checkMemberBankExist(Long memberId);
}

package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.entity.MemberCrypto;
import com.c88.payment.pojo.form.MemberCryptoAddForm;
import com.c88.payment.pojo.form.MemberCryptoModifyForm;
import com.c88.payment.pojo.vo.MemberCryptoVO;

import java.util.List;

/**
 * @author user
 * @description 针对表【payment_member_virtual_bank(會員虛擬銀行)】的数据库操作Service
 * @createDate 2022-05-26 11:43:15
 */
public interface IMemberCryptoService extends IService<MemberCrypto> {

    List<MemberCryptoVO> findMemberCrypto(Long memberId);

    Boolean addMemberCrypto(Long memberId, MemberCryptoAddForm form);

    Boolean modifyMemberCrypto(MemberCryptoModifyForm form);

    Boolean deleteMemberCrypto(Integer id);

    Boolean checkMemberCryptoExist(Long memberId);
}

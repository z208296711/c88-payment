package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.common.web.exception.BizException;
import com.c88.payment.mapper.MemberCryptoMapper;
import com.c88.payment.mapstruct.MemberCryptoConverter;
import com.c88.payment.pojo.entity.MemberCrypto;
import com.c88.payment.pojo.form.MemberCryptoAddForm;
import com.c88.payment.pojo.form.MemberCryptoModifyForm;
import com.c88.payment.pojo.vo.MemberCryptoVO;
import com.c88.payment.service.IMemberCryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author user
 * @description 针对表【payment_member_virtual_bank(會員虛擬銀行)】的数据库操作Service实现
 * @createDate 2022-05-26 11:43:15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCryptoServiceImpl extends ServiceImpl<MemberCryptoMapper, MemberCrypto>
        implements IMemberCryptoService {

    private final MemberCryptoConverter memberCryptoConverter;

    @Override
    public List<MemberCryptoVO> findMemberCrypto(Long memberId) {
        return this.lambdaQuery()
                .eq(MemberCrypto::getMemberId, memberId)
                .list()
                .stream()
                .map(memberCryptoConverter::toVo)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean addMemberCrypto(Long memberId, MemberCryptoAddForm form) {
        // 檢查會員提幣地址是否重複
        checkCryptoAddressIsExist(form.getAddress());

        return this.save(
                MemberCrypto.builder()
                        .memberId(memberId.intValue())
                        .nickname(form.getNickname())
                        .protocol(form.getProtocol())
                        .address(form.getAddress())
                        .build()
        );
    }

    @Override
    public Boolean modifyMemberCrypto(MemberCryptoModifyForm form) {
        // 檢查會員提幣地址是否重複
        checkCryptoAddressIsExist(form.getAddress());

        return this.updateById(
                MemberCrypto.builder()
                        .nickname(form.getNickname())
                        .protocol(form.getProtocol())
                        .address(form.getAddress())
                        .enable(form.getEnable())
                        .build()
        );
    }

    @Override
    public Boolean deleteMemberCrypto(Integer id) {
        return this.removeById(id);
    }

    @Override
    public Boolean checkMemberCryptoExist(Long memberId) {
        return !this.lambdaQuery()
                .eq(MemberCrypto::getMemberId, memberId)
                .list()
                .isEmpty();
    }

    private void checkCryptoAddressIsExist(String address) {
        this.lambdaQuery()
                .eq(MemberCrypto::getAddress, address)
                .oneOpt()
                .ifPresent(x -> {
                    throw new BizException("A0218");
                });
    }
}







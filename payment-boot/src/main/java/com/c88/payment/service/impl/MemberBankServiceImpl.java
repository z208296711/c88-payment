package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.common.core.result.Result;
import com.c88.common.web.exception.BizException;
import com.c88.member.api.H5MemberFeignClient;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.payment.mapper.MemberBankMapper;
import com.c88.payment.mapstruct.MemberBankConverter;
import com.c88.payment.pojo.entity.MemberBank;
import com.c88.payment.pojo.form.MemberBankAddForm;
import com.c88.payment.pojo.form.MemberBankModifyForm;
import com.c88.payment.pojo.vo.MemberBankVO;
import com.c88.payment.service.IMemberBankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author user
 * @description 针对表【payment_member_bank(會員銀行)】的数据库操作Service实现
 * @createDate 2022-05-26 11:43:15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberBankServiceImpl extends ServiceImpl<MemberBankMapper, MemberBank>
        implements IMemberBankService {

    private final MemberBankConverter memberBankConverter;

    private final MemberFeignClient memberFeignClient;

    private final H5MemberFeignClient h5MemberFeignClient;

    @Override
    public List<MemberBankVO> findMemberBank(Long memberId) {
        return this.lambdaQuery()
                .eq(MemberBank::getMemberId, memberId)
                .list()
                .stream()
                .map(memberBankConverter::toVo)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean addMemberBank(Long memberId, MemberBankAddForm form) {
        // 檢查銀行卡不得重複
        checkBankCardIsExist(form.getCardNo());

        // 檢查無原真實姓名時寫入真實姓名,反之檢查真實姓名
        Result<MemberInfoDTO> memberResult = memberFeignClient.getMemberInfo(memberId);
        if (Result.isSuccess(memberResult)) {
            MemberInfoDTO member = memberResult.getData();
            String realName = member.getRealName();
            if (StringUtils.hasText(realName)) {
                if (!realName.equals(form.getRealName())) {
                    throw new BizException("A0219");
                }
            } else {
                h5MemberFeignClient.modifyMemberInfo(memberId, form.getRealName());
            }
        }

        return this.save(
                MemberBank.builder()
                        .memberId(memberId.intValue())
                        .realName(form.getRealName())
                        .bankId(form.getBankId())
                        .cardNo(form.getCardNo())
                        .build()
        );
    }

    @Override
    public Boolean modifyMemberBank(Long memberId, MemberBankModifyForm form) {
        return this.updateById(
                MemberBank.builder()
                        .id(form.getId())
                        .bankId(form.getBankId())
                        .realName(form.getRealName())
                        .cardNo(form.getCardNo())
                        .enable(form.getEnable())
                        .build()
        );
    }

    @Override
    public Boolean deleteMemberBank(Integer id) {
        return this.removeById(id);
    }

    @Override
    public Boolean checkMemberBankExist(Long memberId) {
        return !this.lambdaQuery()
                .eq(MemberBank::getMemberId, memberId)
                .list()
                .isEmpty();
    }

    private void checkBankCardIsExist(String cardNo) {
        this.lambdaQuery()
                .eq(MemberBank::getCardNo, cardNo)
                .oneOpt()
                .ifPresent(x -> {
                    throw new BizException("A0217");
                });
    }
}





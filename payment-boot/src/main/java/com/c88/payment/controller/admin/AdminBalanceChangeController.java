package com.c88.payment.controller.admin;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.enums.BalanceChangeTypeLinkEnum;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.payment.enums.BalanceChangeTypeEnum;
import com.c88.payment.mapstruct.BalanceChangeRecordConverter;
import com.c88.payment.pojo.entity.BalanceChangeRecord;
import com.c88.payment.pojo.entity.MemberBalance;
import com.c88.payment.pojo.entity.MemberWithdrawBet;
import com.c88.payment.pojo.form.AddBalanceChangeForm;
import com.c88.payment.pojo.form.AdminSearchMemberBalanceChangeForm;
import com.c88.payment.pojo.vo.AdminBalanceChangeRecordVO;
import com.c88.payment.pojo.vo.WithdrawDistanceVO;
import com.c88.payment.service.IBalanceChangeRecordService;
import com.c88.payment.service.IMemberBalanceService;
import com.c88.payment.service.IMemberWithdrawBetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "『後台』會員詳情-帳變紀錄")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/balance/change")
public class AdminBalanceChangeController {

    private final IMemberBalanceService iMemberBalanceService;
    private final IMemberWithdrawBetService iMemberWithdrawBetService;
    private final IBalanceChangeRecordService iBalanceChangeRecordService;
    private final BalanceChangeRecordConverter balanceChangeRecordConverter;

    @Operation(summary = "帳變紀錄-查詢")
    @GetMapping
    public PageResult<AdminBalanceChangeRecordVO> findBalanceChangeRecordPage(@ParameterObject AdminSearchMemberBalanceChangeForm form) {
        LambdaQueryChainWrapper<BalanceChangeRecord> wrapper = iBalanceChangeRecordService.lambdaQuery()
                .eq(BalanceChangeRecord::getMemberId, form.getMemberId())
                .le(StringUtils.isNotBlank(form.getEndTime()), BalanceChangeRecord::getGmtCreate, form.getEndTime())
                .ge(StringUtils.isNotBlank(form.getStartTime()), BalanceChangeRecord::getGmtCreate, form.getStartTime())
                .in(CollectionUtil.isNotEmpty(form.getTypes()), BalanceChangeRecord::getType, form.getTypes())
                .orderByDesc(BalanceChangeRecord::getGmtCreate)
                .orderByDesc(BalanceChangeRecord::getId);
        List<BalanceChangeRecord> list = wrapper.list();
        BigDecimal balanceChangeTotal = list.stream().map(BalanceChangeRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        IPage<BalanceChangeRecord> balanceChangeRecordPage = wrapper
                .page(new Page<>(form.getPageNum(), form.getPageSize()));
        IPage<AdminBalanceChangeRecordVO> recordVOIPage = balanceChangeRecordPage.convert(balanceChangeRecordConverter::toVo);
        return PageResult.success(recordVOIPage, balanceChangeTotal);
    }

    @Operation(summary = "帳變紀錄-查詢總額")
    @GetMapping("/total")
    public Result<BigDecimal> findBalanceChangeTotal(@ParameterObject AdminSearchMemberBalanceChangeForm form) {
        return Result.success(iBalanceChangeRecordService.findBalanceChangeTotal(form));
    }

    @Operation(summary = "帳變紀錄-新增修改紀錄")
    @PostMapping
    @Transactional
    public Result<Boolean> addBalanceChangeRecord(@RequestBody AddBalanceChangeForm form) {
        MemberBalance memberInfoDTOResult = iMemberBalanceService.findByMemberId(form.getMemberId());
        MemberWithdrawBet memberWithdrawBet = iMemberWithdrawBetService.findMemberWithdrawBet(memberInfoDTOResult.getMemberId(), memberInfoDTOResult.getUsername());
        memberWithdrawBet.setAccBet(memberWithdrawBet.getAccBet().add(form.getAmount()));
        if (BigDecimal.ZERO.compareTo(memberWithdrawBet.getAccBet()) > 0) {
            memberWithdrawBet.setAccBet(BigDecimal.ZERO);
        }
        iMemberWithdrawBetService.updateById(memberWithdrawBet);

        BalanceChangeRecord balanceChangeRecord = new BalanceChangeRecord();
        balanceChangeRecord.setMemberId(memberWithdrawBet.getMemberId());
        balanceChangeRecord.setUsername(memberWithdrawBet.getUsername());
        balanceChangeRecord.setType(BalanceChangeTypeEnum.ADJUST.getValue());
        balanceChangeRecord.setNote(BalanceChangeTypeLinkEnum.ADJUST.getI18n());
        balanceChangeRecord.setAmount(BigDecimal.ZERO);
        balanceChangeRecord.setCurrentBalance(memberInfoDTOResult.getBalance());
        balanceChangeRecord.setValidBet(BigDecimal.ZERO);
        balanceChangeRecord.setBetRate(null);
        balanceChangeRecord.setAccBet(memberWithdrawBet.getAccBet());
        return Result.success(iBalanceChangeRecordService.save(balanceChangeRecord));
    }

    @Operation(summary = "帳變紀錄-當前玩家累計流水")
    @GetMapping("/{memberId}/acc/bet")
    public Result<WithdrawDistanceVO> getAccRecord(@PathVariable Long memberId) {
        MemberWithdrawBet memberWithdrawBet = iMemberWithdrawBetService.lambdaQuery()
                .eq(MemberWithdrawBet::getMemberId, memberId)
                .oneOpt()
                .orElse(MemberWithdrawBet.builder().accBet(BigDecimal.ZERO).validBet(BigDecimal.ZERO).build());
//                .map(MemberWithdrawBet::getAccBet)
//                .orElse(BigDecimal.ZERO);
        BigDecimal distance = memberWithdrawBet.getAccBet().subtract(memberWithdrawBet.getValidBet());
        distance = distance.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO : distance;
        WithdrawDistanceVO withdrawDistanceVO = WithdrawDistanceVO.builder().accBet(memberWithdrawBet.getAccBet()).distance(distance).build();
        return Result.success(withdrawDistanceVO);
    }

}

package com.c88.payment.controller.h5;

import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.affiliate.api.dto.AffiliateInfoDTO;
import com.c88.affiliate.api.feign.AffiliateFeignClient;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.AffiliateUtils;
import com.c88.payment.enums.AffiliateBalanceChangeTypeEnum;
import com.c88.payment.mapstruct.AffiliateBalanceChangeRecordConverter;
import com.c88.payment.pojo.entity.AffiliateBalance;
import com.c88.payment.pojo.entity.AffiliateBalanceChangeRecord;
import com.c88.payment.pojo.form.*;
import com.c88.payment.pojo.vo.AffiliateBalanceChangeRecordVO;
import com.c88.payment.pojo.vo.AffiliateMemberTransferRecordVO;
import com.c88.payment.pojo.vo.AffiliateTransferRecordVO;
import com.c88.payment.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ExecutionException;

@Tag(name = "代理會員標籤")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/h5/affiliate")
public class H5AffiliateController {

    private final IAffiliateTransferService iAffiliateTransferService;

    private final IAffiliateWithdrawService iAffiliateWithdrawService;

    private final IAffiliateBalanceService iAffiliateBalanceService;

    private final IAffiliateBalanceChangeRecordService iAffiliateBalanceChangeRecordService;

    private final AffiliateBalanceChangeRecordConverter affiliateBalanceChangeRecordConverter;

    private final AffiliateFeignClient affiliateFeignClient;

    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "代理佣金(餘額)")
    @PostMapping("/balance")
    public Result<BigDecimal> balance(@RequestBody CheckAffiliateBalanceForm form) {
        Long affiliateId = AffiliateUtils.getAffiliateId();
        if (affiliateId == null) {
            throw new BizException(ResultCode.ACCESS_UNAUTHORIZED);
        }
        Result<AffiliateInfoDTO> affiliateInfoDTOResult = affiliateFeignClient.getAffiliateInfoById(affiliateId);
        if (!Result.isSuccess(affiliateInfoDTOResult)) {
            throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }
        AffiliateInfoDTO affiliateInfoDTO = affiliateInfoDTOResult.getData();
        if (!passwordEncoder.matches(form.getWithdrawPassword(), affiliateInfoDTO.getWithdrawPassword())) {
            throw new BizException(ResultCode.WITHDRAW_PASSWORD_ERROR);
        }
        AffiliateBalance affiliateBalance = iAffiliateBalanceService.findByAffiliateId(affiliateId);
        return Result.success(affiliateBalance.getBalance());
    }


    @Operation(summary = "代理-提款申請")
    @PostMapping("/withdraw")
    public Result<Boolean> withdrawApply(HttpServletRequest request, @Valid @RequestBody AffiliateWithdrawApplyForm form) {
        Long affiliateId = AffiliateUtils.getAffiliateId();
        if (affiliateId == null) {
            throw new BizException(ResultCode.ACCESS_UNAUTHORIZED);
        }
        return Result.success(iAffiliateWithdrawService.withdrawApply(affiliateId, ServletUtil.getClientIP(request), form));
    }

    @Operation(summary = "代理佣金轉帳")
    @PostMapping("/transfer")
    public Result<Boolean> transfer(HttpServletRequest request, @Valid @RequestBody AffiliateTransferForm form) throws ExecutionException, InterruptedException {
        Long affiliateId = AffiliateUtils.getAffiliateId();
        if (affiliateId == null) {
            throw new BizException(ResultCode.ACCESS_UNAUTHORIZED);
        }
        String username = AffiliateUtils.getUsername();
        if (username.equalsIgnoreCase(form.getUsername())) {
            throw new BizException(ResultCode.AFFILIATE_CAN_NOT_TRANSFER_TO_MYSELF);
        }
        return Result.success(iAffiliateTransferService.transfer(affiliateId, ServletUtil.getClientIP(request), form));
    }

    @Operation(summary = "查詢-代理佣金轉帳-紀錄")
    @GetMapping("/transfer/record")
    public PageResult<AffiliateTransferRecordVO> transfer(@ParameterObject SearchAffiliateTransferForm form) {
        Long affiliateId = AffiliateUtils.getAffiliateId();
        if (affiliateId == null) {
            throw new BizException(ResultCode.ACCESS_UNAUTHORIZED);
        }
        return PageResult.success(iAffiliateTransferService.findTransferRecordPage(affiliateId, form));
    }


    @Operation(summary = "查詢-會員佣金轉帳-紀錄")
    @GetMapping("/member/transfer/record")
    public PageResult<AffiliateMemberTransferRecordVO> memberTransfer(@ParameterObject SearchAffiliateTransferForm form) {
        Long affiliateId = AffiliateUtils.getAffiliateId();

        if (affiliateId == null) {
            throw new BizException(ResultCode.ACCESS_UNAUTHORIZED);
        }
        return PageResult.success(iAffiliateTransferService.findMemberTransferRecordPage(affiliateId, form));
    }

    @Operation(summary = "代理-會員轉帳")
    @PostMapping("/member/transfer")
    public Result<Boolean> affiliateToMemberTransfer(HttpServletRequest request,
                                                     @Valid @RequestBody AffiliateTransferForm form) throws ExecutionException, InterruptedException {
        Long affiliateId = AffiliateUtils.getAffiliateId();
        if (affiliateId == null) {
            throw new BizException(ResultCode.ACCESS_UNAUTHORIZED);
        }
        return Result.success(iAffiliateTransferService.memberTransfer(affiliateId, ServletUtil.getClientIP(request), form));
    }

    @Operation(summary = "查詢-代理帳變-紀錄")
    @GetMapping("/balance/change/record")
    public PageResult<AffiliateBalanceChangeRecordVO> balanceChange(@ParameterObject SearchAffiliateBalanceChangeForm form) {
        Long affiliateId = AffiliateUtils.getAffiliateId();
        if (affiliateId == null) {
            throw new BizException(ResultCode.ACCESS_UNAUTHORIZED);
        }
        boolean showBalance = false;
        if (StringUtils.isNotBlank(form.getWithdrawPassword())) {
            Result<AffiliateInfoDTO> affiliateInfoDTOResult = affiliateFeignClient.getAffiliateInfoById(affiliateId);
            if (!Result.isSuccess(affiliateInfoDTOResult)) {
                throw new BizException(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
            }

            AffiliateInfoDTO affiliateInfoDTO = affiliateInfoDTOResult.getData();
            if (!passwordEncoder.matches(form.getWithdrawPassword(), affiliateInfoDTO.getWithdrawPassword())) {
                throw new BizException(ResultCode.WITHDRAW_PASSWORD_ERROR);
            }
            showBalance = true;
        }
        //管理員加值算在微調
        if(form.getTypeList().contains(AffiliateBalanceChangeTypeEnum.TUNING.getValue()))form.getTypeList().add(AffiliateBalanceChangeTypeEnum.ADMIN_RECHARGE.getValue());
        boolean finalShowBalance = showBalance;
        return PageResult.success(iAffiliateBalanceChangeRecordService
                .lambdaQuery()
                .eq(AffiliateBalanceChangeRecord::getAffiliateId, affiliateId)
                .ge(form.getStartTime() != null, AffiliateBalanceChangeRecord::getGmtCreate, LocalDateTime.of(form.getStartTime(), LocalTime.MIN).minusHours(form.getGmtTime()))
                .le(form.getEndTime() != null, AffiliateBalanceChangeRecord::getGmtCreate, LocalDateTime.of(form.getEndTime(), LocalTime.MAX).minusHours(form.getGmtTime()))
                .in(CollectionUtils.isNotEmpty(form.getTypeList()), AffiliateBalanceChangeRecord::getType, form.getTypeList())
                .orderByDesc(AffiliateBalanceChangeRecord::getGmtCreate)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(x -> affiliateBalanceChangeRecordConverter.toVo(x, finalShowBalance)));
    }


}

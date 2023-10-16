package com.c88.payment.controller.admin;

import com.c88.common.core.result.Result;
import com.c88.common.web.util.UUIDUtils;
import com.c88.payment.dto.AddAffiliateBalanceDTO;
import com.c88.payment.dto.AffiliateBalanceChangeRecordDTO;
import com.c88.payment.dto.PaymentAffiliateBalanceDTO;
import com.c88.payment.enums.AffiliateBalanceChangeTypeEnum;
import com.c88.payment.pojo.form.AddAffiliateBalanceByAdminForm;
import com.c88.payment.service.IAffiliateBalanceChangeRecordService;
import com.c88.payment.service.IAffiliateBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "『後台』代理-銀行管理列表")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/affiliate/balance")
public class AdminAffiliateBalanceController {

    private final IAffiliateBalanceService iAffiliateBalanceService;

    private final IAffiliateBalanceChangeRecordService iAffiliateBalanceChangeRecordService;

    @Operation(summary = "新增/減少 代理金額")
    @PostMapping
    public Result<BigDecimal> addAffiliateBalance(@Validated @RequestBody AddAffiliateBalanceDTO dto) {
        return Result.success(iAffiliateBalanceService.addBalance(dto));
    }

    @Operation(summary = "新增/減少 代理金額")
    @GetMapping("/affiliateId")
    public Result<List<PaymentAffiliateBalanceDTO>> findPaymentAffiliateBalanceByAffiliateIdArray(@RequestParam List<Long> affiliateIds) {
        return Result.success(iAffiliateBalanceService.findPaymentAffiliateBalanceDTO(affiliateIds));
    }

    @Operation(summary = "管理員充值")
    @PutMapping("/admin/add")
    public Result<BigDecimal> addAffiliateBalanceByAdmin(@Validated @RequestBody AddAffiliateBalanceByAdminForm form) {
        return Result.success(
                iAffiliateBalanceService.addAdminBalance(AddAffiliateBalanceDTO.builder()
                        .affiliateId(form.getId())
                        .serialNo(UUIDUtils.genOrderId("AD"))
                        .amount(form.getAmount())
                        .type(AffiliateBalanceChangeTypeEnum.ADMIN_RECHARGE.getValue())
                        .note(form.getNote())
                        .build())
        );
    }

    @Operation(summary = "帳變紀錄-查詢各代理管理員充值")
    @GetMapping("/date")
    public Result<List<AffiliateBalanceChangeRecordDTO>> findBalanceChangeFromDate(@RequestParam() List<Long> affiliateIds,
                                                                                   @RequestParam()@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                                                   @RequestParam()@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(iAffiliateBalanceChangeRecordService.findChangeRecordByAffiliateIdDate(affiliateIds,startTime,endTime));
    }

}

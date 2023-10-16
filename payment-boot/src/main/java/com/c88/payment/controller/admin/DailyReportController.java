package com.c88.payment.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.c88.common.core.result.PageResult;
import com.c88.payment.pojo.form.SearchRechargeWithdrawForm;
import com.c88.payment.pojo.vo.DailyRechargeWithdrawVO;
import com.c88.payment.service.IBalanceChangeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "報表")
@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class DailyReportController {


    private final IBalanceChangeRecordService iBalanceChangeRecordService;

    @Operation(summary = "會員每日存提報表-查詢")
    @GetMapping("/{memberId}/daily/recharge/withdraw")
    public PageResult<DailyRechargeWithdrawVO> findRechargeWithdrawPage(@PathVariable Long memberId,
                                                                        @ParameterObject SearchRechargeWithdrawForm form) {
        IPage<DailyRechargeWithdrawVO> page = iBalanceChangeRecordService.findDailyRechargeWithdrawReport(memberId, form);
        return PageResult.success(page);
    }
}

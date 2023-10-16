package com.c88.payment.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.OperationEnum;
import com.c88.member.vo.OptionVO;
import com.c88.payment.dto.BankDTO;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.form.*;
import com.c88.payment.pojo.vo.AdminBankVO;
import com.c88.payment.service.IBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "『後台』財務-銀行管理列表")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bank")
public class AdminBankController {

    private final IBankService iBankService;

    @Operation(summary = "銀行管理列表-查詢")
    @GetMapping
    public PageResult<AdminBankVO> findBank(@ParameterObject AdminSearchBankForm form) {
        return PageResult.success(iBankService.findAdminBankPage(form));
    }

    @Operation(summary = "修改銀行排序")
    @PutMapping("/sort")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#form.code,#form.sort}",
            menu = "menu.finance",
            menuPage = "menu.bank-list",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_bank.operation_log05")
    public Result<Boolean> modifyPlatformGameSort(@Validated @RequestBody ModifyBankSortForm form) {
        form.setSort(iBankService.count(new LambdaQueryWrapper<Bank>()
                .le(Bank::getSort, form.getSort())));// 修正排序序號
        return Result.success(iBankService.modifyBankSort(form));
    }

    @Operation(summary = "修改銀行排序 置頂置底", description = "0置頂, 1置底")
    @PutMapping("/sort/top/bottom")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#form.code}",
            menu = "menu.finance",
            menuPage = "menu.bank-list",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "#form.sortType==0?'be_bank.operation_log06':'be_bank.operation_log07'")
    public Result<Boolean> modifyBankSortTopBottom(@Valid @RequestBody ModifyBankSortTopBottomForm form) {
        form.setCode(iBankService.getById(form.getId()).getCode());
        return Result.success(iBankService.modifyBankSortTopBottom(form));
    }

    @Operation(summary = "銀行管理列表-新增銀行")
    @PostMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.INSERT,
            content = "new String[]{#form.code}",
            menu = "menu.finance",
            menuPage = "menu.bank-list",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_bank.operation_log01")
    public Result<Boolean> addBank(@RequestBody @Validated AddBankForm form) {
        return Result.success(iBankService.addBank(form));
    }

    @Operation(summary = "銀行管理列表-修改銀行")
    @PutMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#form.code}",
            menu = "menu.finance",
            menuPage = "menu.bank-list",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_bank.operation_log02")
    public Result<Boolean> modifyBank(@RequestBody @Validated ModifyBankForm form) {
        return Result.success(iBankService.modifyBank(form));
    }

    @Operation(summary = "銀行管理列表-維護銀行設定")
    @PutMapping("/maintain")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "#form.dailyEnable==0?new String[]{#form.code,#dateTimeFormatter.format(#form.assignStartTime),#dateTimeFormatter.format(#form.assignEndTime)}:"
                    + "new String[]{#form.code,'${form.dailyEnable}'.equals('0')?'停用':'啟用',#timeFormatter.format(#form.dailyStartTime),#timeFormatter.format(#form.dailyEndTime)}",
            menu = "menu.finance",
            menuPage = "menu.bank-list",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "#form.dailyEnable==0?'be_bank.operation_log03':'be_bank.operation_log04'")
    public Result<Boolean> maintainBank(@RequestBody @Validated MaintainBankForm form) {
        return Result.success(iBankService.modifyBankMaintainTime(form));
    }

    @Operation(summary = "銀行選項-查找")
    @GetMapping("/option")
    public Result<List<OptionVO<Long>>> findBankOption() {
        return Result.success(iBankService.findBankOption());
    }

    @Operation(summary = "銀行選項-查找 By商戶")
    @GetMapping("/merchant/{merchantId}/option")
    public Result<List<OptionVO<Long>>> findBankOptionByMerchantIdOption(@PathVariable("merchantId") Long merchantId) {
        return Result.success(iBankService.findBankOptionByMerchantIdOption(merchantId));
    }

    @Operation(summary = "銀行選項-查找")
    @GetMapping("/dto")
    public Result<List<BankDTO>> findBankDTO() {
        return Result.success(iBankService.findBankDTO());
    }
}

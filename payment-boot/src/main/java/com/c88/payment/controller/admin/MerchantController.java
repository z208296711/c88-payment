package com.c88.payment.controller.admin;

import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.OperationEnum;
import com.c88.member.vo.OptionVO;
import com.c88.payment.pojo.form.CheckChannelForm;
import com.c88.payment.pojo.form.FindCheckChannelForm;
import com.c88.payment.pojo.form.FindMerchantPageForm;
import com.c88.payment.pojo.form.ModifyMerchantNoteForm;
import com.c88.payment.pojo.vo.*;
import com.c88.payment.service.IMerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "『後台』支付商戶")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/merchant")
public class MerchantController {

    private final IMerchantService iMerchantService;

    @Operation(summary = "商戶列表-查找")
    @GetMapping
    public PageResult<MerchantVO> findMerchantPage(@ParameterObject FindMerchantPageForm form) {
        return PageResult.success(iMerchantService.findMerchantPage(form));
    }

    @Operation(summary = "商戶-修改")
    @PutMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.name, #form.note==null||#form.note.isBlank()?(#form.enable==0?'停用':'啟用'):#form.note}",
            menu = "menu.finance",
            menuPage = "menu.merchant-list",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "#form.note==null||#form.note.isBlank()?'be_merchant.operation_log01':'be_merchant.operation_log02'")
    public Result<Boolean> modifyMerchant(@Validated @RequestBody ModifyMerchantNoteForm form) {
        form.setName(iMerchantService.getById(form.getId()).getName());
        return Result.success(iMerchantService.modifyMerchant(form));
    }

    @Operation(summary = "商戶狀態設置-清單")
    @GetMapping("/state/{merchantId}")
    public Result<MerchantStateSettingVO> findMerchantState(@PathVariable("merchantId") Integer merchantId) {
        return Result.success(iMerchantService.findMerchantState(merchantId));
    }

    @Operation(summary = "商戶支付方式設置-選項")
    @GetMapping("/setting/option")
    public Result<List<MerchantSettingOptionVO>> findMerchantSettingOption() {
        return Result.success(iMerchantService.findMerchantSettingOption());
    }

    @Operation(summary = "商戶通道-查找")
    @GetMapping("/check/channel")
    public Result<List<CheckChannelVO>> findCheckChannel(@ParameterObject @Validated FindCheckChannelForm form) {
        return Result.success(iMerchantService.findCheckChannel(form));
    }

    @Operation(summary = "商戶通道-測試")
    @PostMapping("/check/channel")
    public Result<Boolean> checkChannel(@RequestBody @Validated CheckChannelForm from, HttpServletRequest request) {
        return Result.success(iMerchantService.checkChannel(from, request));
    }

    @Operation(summary = "商戶-選項")
    @GetMapping("/option")
    public Result<List<OptionVO<Long>>> findMerchantOption() {
        return Result.success(iMerchantService.findMerchantOption());
    }

    @Operation(summary = "商戶設置,支付方式設置-選項")
    @GetMapping("/rechargeType/option")
    public Result<List<MerchantRechargeOptionVO>> findMerchantAndRechargeOption() {
        return Result.success(iMerchantService.findMerchantAndRechargeOptions());
    }

    @Operation(summary = "商戶設置,支付方式設置-選項 by 商戶")
    @GetMapping("/rechargeType/merchant/{merchantId}/option")
    public Result<List<OptionVO<Long>>> findMerchantAndRechargeMerchantOption(@PathVariable("merchantId") Long merchantId) {
        return Result.success(iMerchantService.findMerchantAndRechargeMerchantOption(merchantId));
    }
}
package com.c88.payment.controller.admin;

import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.OperationEnum;
import com.c88.payment.pojo.form.ExportOnlineMemberRechargeForm;
import com.c88.payment.pojo.form.FindOnlineRechargeForm;
import com.c88.payment.pojo.form.OnlineMakeUpForm;
import com.c88.payment.pojo.vo.OnlineMemberRechargeVO;
import com.c88.payment.service.IMemberRechargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Tag(name = "『後台』在線充值相關")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recharge/online")
public class OnlineMemberRechargeController {

    private final IMemberRechargeService iRechargeService;

    @Operation(summary = "查詢在線充值訂單")
    @GetMapping
    public PageResult<OnlineMemberRechargeVO> findOnlineRecharge(@Validated @ParameterObject FindOnlineRechargeForm form) {
        return PageResult.success(iRechargeService.findOnlineRecharge(form));
    }

    @Operation(summary = "在線手動補單")
    @PostMapping("/makeUp")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#form.tradeNo}",
            menu = "menu.finance",
            menuPage = "menu.online-recharge",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_deposit.operation_log01")
    public Result<Boolean> onlineMakeUp(@RequestBody @Validated OnlineMakeUpForm form, HttpServletRequest request) {
        return Result.success(iRechargeService.onlineMakeUp(form,request));
    }

    @Operation(summary = "在線充值報表")
    @PostMapping("/export")
    public void exportOnlineMemberRecharge(@RequestBody @Validated ExportOnlineMemberRechargeForm form, HttpServletResponse response) {
        iRechargeService.exportOnlineMemberRecharge(form,response);
    }

}

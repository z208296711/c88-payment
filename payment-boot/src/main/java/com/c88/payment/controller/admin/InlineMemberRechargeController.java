package com.c88.payment.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.OperationEnum;
import com.c88.payment.pojo.form.InlineMakeUpForm;
import com.c88.payment.pojo.form.FindInlineRechargeForm;
import com.c88.payment.pojo.vo.RechargeInlineExcelVO;
import com.c88.payment.pojo.vo.RechargeVO;
import com.c88.payment.service.IMemberRechargeService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Tag(name = "『後台』充值相關")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recharge/inline")
public class InlineMemberRechargeController {

    private final IMemberRechargeService iRechargeService;

    @Operation(summary = "查詢手動充值訂單")
    @GetMapping
    public PageResult<RechargeVO> queryInlineRecharge(@ParameterObject FindInlineRechargeForm form) {
        return PageResult.success(iRechargeService.findInlineRecharge(form));
    }

    @ApiOperation(value = "手動充值訂單Excel下載")
    @GetMapping("/download")
    public void download(HttpServletResponse response, @ParameterObject FindInlineRechargeForm form) throws IOException {
        String date = LocalDate.now().toString();
        String fileName = URLEncoder.encode("manual_merchant_" + date, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), RechargeInlineExcelVO.class).sheet()
                .doWrite(iRechargeService.findInlineRechargeForExcel(form));
    }

    @Operation(summary = "匯款確認", description = "傳入單號ID")
    @PutMapping(value = "/{rechargeId}")
    public Result<Boolean> checkInlineRecharge(@PathVariable Long rechargeId, @RequestParam String remark) {
        return Result.success(iRechargeService.checkInlineRecharge(rechargeId, remark));
    }

    @Operation(summary = "手動充值-手動補單")
    @PostMapping(value = "/makeUp")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#form.tradeNo}",
            menu = "menu.finance",
            menuPage = "menu.manual-recharge",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_company_deposit.operation_log03")
    public Result<Boolean> createInlineMakeUpRecharge(@RequestBody @Validated InlineMakeUpForm form) {
        return Result.judge(iRechargeService.inlineMakeUp(form));
    }
}

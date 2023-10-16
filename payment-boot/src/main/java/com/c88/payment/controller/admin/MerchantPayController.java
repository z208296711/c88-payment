package com.c88.payment.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.enums.RemitStateEnum;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.LogOpResponse;
import com.c88.common.web.log.OperationEnum;
import com.c88.payment.pojo.entity.MerchantPay;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.FindMerchantPayForm;
import com.c88.payment.pojo.form.ModifyMerchantPayForm;
import com.c88.payment.pojo.vo.MerchantPayVO;
import com.c88.payment.service.IMerchantPayService;
import com.c88.payment.service.IMerchantService;
import com.c88.payment.service.IMemberWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Tag(name = "『後台』出款代付管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/merchant/pay")
public class MerchantPayController {

    private final IMerchantPayService iMerchantPayService;

    private final IMemberWithdrawService iWithdrawService;

    private final IMerchantService iMerchantService;

    @Operation(summary = "查詢代付管理")
    @GetMapping
    public PageResult<MerchantPayVO> findMerchantPay(@ParameterObject FindMerchantPayForm form) {
        List<MemberWithdraw> withdraws = iWithdrawService.lambdaQuery()
                .eq(MemberWithdraw::getRemitState, RemitStateEnum.PAY_PENDING.getState())
                .list();

        Page<MerchantPayVO> merchantPayPage = iMerchantPayService.findMerchantPay(form);

        merchantPayPage.getRecords().forEach(merchantPay -> {
                    BigDecimal processBalance = withdraws.stream()
                            .filter(filter -> filter.getMerchantId().intValue() == merchantPay.getId())
                            .map(MemberWithdraw::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    merchantPay.setProcessBalance(processBalance);

                }
        );

        return PageResult.success(merchantPayPage);
    }

    @Operation(summary = "更新代付管理")
    @PutMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.merchantName," +
                    "#form.enable!=null?(#form.enable==0?'停用':'啟用') : (#form.agentWithdrawEnable!=null ? (#form.agentWithdrawEnable==0 ? '停用':'啟用') : '')} ",
            menu = "menu.finance",
            menuPage = "menu.pay-on-behalf",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "#form.enable!=null?'be_third_withdraw.operation_log02' : (#form.agentWithdrawEnable!=null ? 'be_third_withdraw.operation_log03' : 'be_third_withdraw.operation_log01')")
    public Result<LogOpResponse> modifyMerchantPay(@RequestBody @Validated ModifyMerchantPayForm form) {
        form.setMerchantName(iMerchantService.getMerchantById(form.getId().longValue()).getName());
        LogOpResponse response = new LogOpResponse();
        MerchantPay before = iMerchantPayService.lambdaQuery().eq(MerchantPay::getId, form.getId()).one();
        response.setBefore(before);
        response.setAfter(form);

        iMerchantPayService.modifyMerchantPay(form);
        return Result.success(response);
    }

    @Operation(summary = "出款代付使用的所有VIP ID")
    @GetMapping("/use/vip")
    public Result<Set<Integer>> findMerchantPayUseVipIds() {
        return Result.success(iMerchantPayService.findMerchantPayUseVipIds());
    }

}

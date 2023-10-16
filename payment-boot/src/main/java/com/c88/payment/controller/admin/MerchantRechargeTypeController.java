package com.c88.payment.controller.admin;

import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.OperationEnum;
import com.c88.payment.pojo.entity.MerchantRechargeType;
import com.c88.payment.pojo.form.ModifyMerchantRechargeType;
import com.c88.payment.pojo.vo.MerchantRechargeTypeVO;
import com.c88.payment.service.IMerchantRechargeTypeService;
import com.c88.payment.service.IMerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "『後台』支付商戶 充值方式")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/merchant/recharge/type")
public class MerchantRechargeTypeController {

    private final IMerchantRechargeTypeService iMerchantRechargeTypeService;
    private final IMerchantService iMerchantService;

    @Operation(summary = "商戶支付方式-清單")
    @GetMapping("/{merchantId}")
    public Result<List<MerchantRechargeTypeVO>> findMerchantRechargeType(@PathVariable("merchantId") Integer merchantId) {
        return Result.success(iMerchantRechargeTypeService.findMerchantRechargeType(merchantId));
    }

    @Operation(summary = "商戶支付方式-修改")
    @PutMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.merchantName, #form.rechargeTypeName," +
                    "#form.enable!=null?(#form.enable==0?'停用':'啟用'):#form.feeRate!=null?#form.feeRate:" +
                    "(#form.maxAmount!=null?#form.maxAmount:#form.note),#form.minAmount!=null?#form.minAmount:''}",
            menu = "menu.finance",
            menuPage = "menu.merchant-list",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "#form.enable!=null?(#form.isBank!=1?'be_merchant.operation_log03':'be_merchant.operation_log04'):"
                    + "#form.feeRate!=null?'be_payment.operation_log02':(#form.maxAmount!=null?'be_payment.operation_log03':'be_payment.operation_log01')")
    public Result<Boolean> modifyMerchantRechargeType(@Validated @RequestBody ModifyMerchantRechargeType form) {
        MerchantRechargeType merchantRechargeType = iMerchantRechargeTypeService.getById(form.getId());
        form.setRechargeTypeName("{{" + merchantRechargeType.getRechargeTypeName() + "}}");// recharge type name 為 i18n，需加{{}}給前端取代
        form.setIsBank(merchantRechargeType.getIsBank());
        form.setMerchantName(iMerchantService.getById(merchantRechargeType.getMerchantId()).getName());
        return Result.success(iMerchantRechargeTypeService.modifyMerchantRechargeType(form));
    }

}

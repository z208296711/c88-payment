package com.c88.payment.controller.admin;

import com.c88.common.core.result.Result;
import com.c88.payment.pojo.form.ModifyMerchantRechargeChannelForm;
import com.c88.payment.service.IMerchantRechargeChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "『後台』支付方式通道")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/merchant/recharge/channel")
public class MerchantRechargeChannelController {

    private final IMerchantRechargeChannelService iMerchantRechargeChannelService;

    @Operation(summary = "商戶-修改")
    @PutMapping
    public Result<Boolean> modifyMerchantRechargeChannel(@RequestBody @Validated ModifyMerchantRechargeChannelForm form) {
        return Result.success(iMerchantRechargeChannelService.modifyMerchantRechargeChannel(form));
    }

}

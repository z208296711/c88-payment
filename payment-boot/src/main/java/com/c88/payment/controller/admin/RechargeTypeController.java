package com.c88.payment.controller.admin;

import com.c88.common.core.result.Result;
import com.c88.member.vo.OptionVO;
import com.c88.payment.service.IRechargeTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "『後台』充值類型")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recharge/type")
public class RechargeTypeController {

    private final IRechargeTypeService iRechargeTypeService;

    @Operation(summary = "商戶支付方式-選單")
    @GetMapping("/option")
    public Result<List<OptionVO<Integer>>> findRechargeTypeOption() {
        return Result.success(iRechargeTypeService.findRechargeTypeOption());
    }

    @Operation(summary = "商戶支付方式-選單-vip篩選")
    @GetMapping("/option/{vipId}")
    public Result<List<OptionVO<Integer>>> findRechargeTypeOption(@PathVariable("vipId") Integer vipId) {
        return Result.success(iRechargeTypeService.findRechargeTypeOptionByVip(vipId));
    }

}

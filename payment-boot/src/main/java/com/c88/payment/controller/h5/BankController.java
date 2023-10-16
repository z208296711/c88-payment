package com.c88.payment.controller.h5;

import com.c88.common.core.result.Result;
import com.c88.payment.pojo.vo.BankVO;
import com.c88.payment.service.IBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "『前台』 銀行")
@RestController
@RequiredArgsConstructor
@RequestMapping("/h5/bank")
public class BankController {

    private final IBankService iBankService;

    @Operation(summary = "查詢銀行")
    @GetMapping
    public Result<List<BankVO>> findBank() {
        return Result.success(iBankService.findBank());
    }

}

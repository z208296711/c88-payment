package com.c88.payment.controller.h5;

import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.AffiliateUtils;
import com.c88.payment.pojo.form.AffiliateBankCardForm;
import com.c88.payment.pojo.vo.AffiliateBankCardVO;
import com.c88.payment.service.IAffiliateBankCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "代理會員標籤")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/h5/affiliate/bank/card")
public class H5AffiliateBankCardController {

    private final IAffiliateBankCardService iAffiliateBankCardService;

    @Operation(summary = "查詢-代理銀行卡")
    @GetMapping
    public Result<List<AffiliateBankCardVO>> findAffiliateBank() {
        Long affiliateId = AffiliateUtils.getAffiliateId();
        if (affiliateId == null) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(iAffiliateBankCardService.findAffiliateBankCard(affiliateId));
    }

    @Operation(summary = "新增-代理銀行卡")
    @PostMapping
    public Result<Boolean> addAffiliateBank(@Valid @RequestBody AffiliateBankCardForm form) {
        Long affiliateId = AffiliateUtils.getAffiliateId();
        if (affiliateId == null) {
            throw new BizException(ResultCode.ACCESS_UNAUTHORIZED);
        }
        return Result.success(iAffiliateBankCardService.addAffiliateBankCard(affiliateId, form));
    }

    @Operation(summary = "解綁-代理銀行卡")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteBankCard(@PathVariable Long id) {
        Long affiliateId = AffiliateUtils.getAffiliateId();
        if (affiliateId == null) {
            throw new BizException(ResultCode.ACCESS_UNAUTHORIZED);
        }
        return Result.success(iAffiliateBankCardService.deleteBankCard(id));
    }

}

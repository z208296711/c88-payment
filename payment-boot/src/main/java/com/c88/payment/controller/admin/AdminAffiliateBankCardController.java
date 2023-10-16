package com.c88.payment.controller.admin;

import com.c88.common.core.result.Result;
import com.c88.payment.pojo.entity.AffiliateBankCard;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.vo.AdminAffiliateBankCardVO;
import com.c88.payment.service.IAffiliateBankCardService;
import com.c88.payment.service.IBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "後台 代理銀行卡")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/affiliate/bank/card")
public class AdminAffiliateBankCardController {

    private final IBankService iBankService;

    private final IAffiliateBankCardService iAffiliateBankCardService;

    @Operation(summary = "查詢-代理銀行卡")
    @GetMapping("/{id}")
    public Result<List<AdminAffiliateBankCardVO>> findAffiliateBankCard(@PathVariable("id") Long affiliateId) {
        Map<Long, String> bankMap = iBankService.lambdaQuery()
                .select(Bank::getId, Bank::getCode)
                .list()
                .stream()
                .collect(Collectors.toMap(Bank::getId, Bank::getCode));

        List<AdminAffiliateBankCardVO> cardVOS = iAffiliateBankCardService.lambdaQuery()
                .eq(AffiliateBankCard::getAffiliateId, affiliateId)
                .list()
                .stream()
                .map(x -> AdminAffiliateBankCardVO.builder()
                        .type(1)
                        .typeName(bankMap.getOrDefault(x.getBankId(), ""))
                        .realName(x.getRealName())
                        .cardNo(x.getCardNo())
                        .build()
                )
                .collect(Collectors.toList());

        return Result.success(cardVOS);
    }

}

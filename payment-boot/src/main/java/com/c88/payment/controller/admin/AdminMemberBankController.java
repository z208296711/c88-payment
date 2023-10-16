package com.c88.payment.controller.admin;

import com.c88.common.core.result.Result;
import com.c88.payment.pojo.entity.MemberBank;
import com.c88.payment.service.IMemberBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "『後台』會員銀行卡")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member/bank")
public class AdminMemberBankController {

    private final IMemberBankService iMemberBankService;

    @Operation(summary = "找匹配到銀行卡會員的ID", description = "只匹配最後的銀行碼")
    @GetMapping("/fuzzy/last/{card}")
    public Result<List<Integer>> findMemberBankFuzzyLast(@PathVariable("card") String card) {
        return Result.success(iMemberBankService.lambdaQuery()
                .likeLeft(MemberBank::getCardNo, card)
                .list()
                .stream()
                .map(MemberBank::getMemberId)
                .collect(Collectors.toList())
        );
    }


}

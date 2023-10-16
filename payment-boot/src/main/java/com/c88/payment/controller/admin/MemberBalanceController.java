package com.c88.payment.controller.admin;

import com.c88.common.core.result.Result;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.dto.MemberBalanceInfoDTO;
import com.c88.payment.dto.PaymentMemberBalanceDTO;
import com.c88.payment.service.IMemberBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "『後台』會員餘額")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member/balance")
public class MemberBalanceController {

    private final IMemberBalanceService iMemberBalanceService;

    @Operation(summary = "取得會員餘額By會員ID")
    @GetMapping("/member/id/{memberId}")
    public Result<PaymentMemberBalanceDTO> findByMemberIdDTO(@PathVariable("memberId") Long memberId) {
        return Result.success(iMemberBalanceService.findByMemberIdDTO(memberId));
    }

    @Operation(summary = "取得會員餘額By會員ID清單")
    @GetMapping("/member/id")
    public Result<List<PaymentMemberBalanceDTO>> findPaymentMemberBalanceByMemberIdArray(@RequestParam List<Long> memberIds) {
        return Result.success(iMemberBalanceService.findByMemberIdArrayDTO(memberIds));
    }

    @Operation(summary = "取得會員餘額By會員帳號")
    @GetMapping("/username/{username}")
    public Result<PaymentMemberBalanceDTO> findByUsernameDTO(@PathVariable("username") String username) {
        return Result.success(iMemberBalanceService.findByUsernameDTO(username));
    }

    @Operation(summary = "新增會員餘額")
    @PutMapping
    public Result<BigDecimal> addBalance(@Validated @RequestBody AddBalanceDTO dto) {
        return Result.success(iMemberBalanceService.addBalance(dto));
    }

    @Operation(summary = "取得會員錢包訊息", description = "會員ID")
    @PostMapping("/info/member/id")
    public Result<List<MemberBalanceInfoDTO>> findMemberBalanceInfoByIds(@RequestBody List<Long> memberIds) {
        return Result.success(iMemberBalanceService.findMemberBalanceInfoByIds(memberIds));
    }

    @Operation(summary = "取得會員錢包訊息", description = "會員帳號")
    @GetMapping("/info/member/username")
    public Result<List<MemberBalanceInfoDTO>> findMemberBalanceInfoByUsernames(@RequestParam List<String> usernames) {
        return Result.success(iMemberBalanceService.findMemberBalanceInfoByUsernames(usernames));
    }

}

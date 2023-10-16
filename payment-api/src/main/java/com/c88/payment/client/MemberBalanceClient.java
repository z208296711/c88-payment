package com.c88.payment.client;

import com.c88.common.core.result.Result;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.dto.MemberBalanceInfoDTO;
import com.c88.payment.dto.PaymentMemberBalanceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "c88-payment", path = "/payment/api/v1/member/balance")
public interface MemberBalanceClient {

    @GetMapping("/member/id/{memberId}")
    Result<PaymentMemberBalanceDTO> findPaymentMemberBalanceByMemberId(@PathVariable("memberId") Long memberId);

    @GetMapping("/username/{username}")
    Result<PaymentMemberBalanceDTO> findPaymentMemberBalanceByUsername(@PathVariable("username") String username);

    @GetMapping("/member/id")
    Result<List<PaymentMemberBalanceDTO>> findPaymentMemberBalanceByMemberIdArray(@RequestParam List<Long> memberIds);

    @PutMapping
    Result<BigDecimal> addBalance(@Validated @RequestBody AddBalanceDTO dto);

    @PostMapping("/info/member/id")
    Result<List<MemberBalanceInfoDTO>> findMemberBalanceInfoByIds(@RequestBody List<Long> memberIds);

    @GetMapping("/info/member/username")
    Result<List<MemberBalanceInfoDTO>> findMemberBalanceInfoByUsernames(@RequestParam List<String> usernames);

}

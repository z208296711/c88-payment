package com.c88.payment.client;

import com.c88.common.core.result.Result;
import com.c88.member.vo.OptionVO;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.dto.BankDTO;
import com.c88.payment.dto.PaymentAffiliateBalanceDTO;
import com.c88.payment.dto.PaymentMemberBalanceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@FeignClient(name = "c88-payment", path = "/payment")
public interface PaymentClient {

    @GetMapping("/api/v1/bank/dto")
    Result<List<BankDTO>> findBankDTO();

    @GetMapping("/api/v1/merchant/pay/use/vip")
    Result<Set<Integer>> findMerchantPayUseVipIds();

    @GetMapping("/api/v1/member/balance/member/id/{memberId}")
    Result<PaymentMemberBalanceDTO> findPaymentMemberBalanceByMemberId(@PathVariable("memberId") Long memberId);

    @GetMapping("/api/v1/member/balance/username/{username}")
    Result<PaymentMemberBalanceDTO> findPaymentMemberBalanceByUsername(@PathVariable("username") String username);

    @GetMapping("/api/v1/member/balance/member/id")
    Result<List<PaymentMemberBalanceDTO>> findPaymentMemberBalanceByMemberIdArray(@RequestParam List<Long> memberIds);

    @GetMapping("/api/v1/affiliate/balance/affiliateId")
    Result<List<PaymentAffiliateBalanceDTO>> findPaymentAffiliateBalanceByAffiliateIdArray(@RequestParam List<Long> affiliateIds);

    @PutMapping("/api/v1/member/balance")
    Result<BigDecimal> addBalance(@Validated @RequestBody AddBalanceDTO dto);

    @GetMapping("/api/v1/recharge/type/option")
    Result<List<OptionVO<Integer>>> findRechargeTypeOption();

    @GetMapping("/api/v1/member/bank/fuzzy/last/{card}")
    Result<List<Integer>> findMemberBankFuzzyLast(@PathVariable("card") String card);

}

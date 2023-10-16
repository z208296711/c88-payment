package com.c88.payment.client;

import com.c88.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "c88-payment", path = "/payment/api/v1/member/bank")
public interface AdminMemberBankClient {

    @GetMapping("/fuzzy/last/{card}")
    Result<List<Integer>> findMemberBankFuzzyLast(@PathVariable("card") String card);

}

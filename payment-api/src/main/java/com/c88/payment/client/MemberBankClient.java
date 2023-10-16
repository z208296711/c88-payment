package com.c88.payment.client;

import com.c88.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "c88-payment", path = "/payment/h5/memberBank")
public interface MemberBankClient {

    @GetMapping("/checkExist")
    Result<Boolean> checkMemberBankExist();

}

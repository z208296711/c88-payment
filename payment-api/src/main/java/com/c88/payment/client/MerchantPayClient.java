package com.c88.payment.client;

import com.c88.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@FeignClient(name = "c88-payment", path = "/payment/api/v1/merchant/pay")
public interface MerchantPayClient {

    @GetMapping("/use/vip")
    Result<Set<Integer>> findMerchantPayUseVipIds();

}

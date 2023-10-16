package com.c88.payment.client;

import com.c88.common.core.result.Result;
import com.c88.payment.dto.AffiliateBalanceChangeRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@FeignClient(name = "c88-payment", path = "/payment/api/v1/affiliate/balance")
public interface AffiliateBalanceChangeRecordClient {

    @GetMapping("/date")
    Result<List<AffiliateBalanceChangeRecordDTO>> findBalanceChangeFromDate(@RequestParam(required = false) List<Long> affiliateIds,
                                                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime);

}
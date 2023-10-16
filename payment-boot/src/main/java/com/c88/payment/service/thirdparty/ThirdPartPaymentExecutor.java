package com.c88.payment.service.thirdparty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
@RequiredArgsConstructor
public class ThirdPartPaymentExecutor {

    private final Map<String, IThirdPartPayService> thirdPartPaymentMap;

    public IThirdPartPayService findByMerchantCode(String merchantCode) {
        if (!thirdPartPaymentMap.containsKey(merchantCode)) {
            throw new IllegalArgumentException("Unknown Merchant Code: " + merchantCode);
        }
        return thirdPartPaymentMap.get(merchantCode);
    }

}

package com.c88.payment.config;

import com.c88.payment.service.thirdparty.IThirdPartPayService;
import com.c88.payment.service.thirdparty.ThirdPartPaymentExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Configuration
public class ThirdPartPaymentConfig {

    @Bean
    public ThirdPartPaymentExecutor requestReceiver(List<IThirdPartPayService> requestHandlers) {
        return new ThirdPartPaymentExecutor(
                requestHandlers.stream()
                        .collect(toMap(IThirdPartPayService::getMerchantCode, Function.identity()))
        );
    }
}

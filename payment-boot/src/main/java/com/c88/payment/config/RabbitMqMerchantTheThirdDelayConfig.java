package com.c88.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqMerchantTheThirdDelayConfig {

    public static final String MERCHANT_THIRD_QUEUE_CANCEL = "merchant_third_queue_cancel";
    public static final String MERCHANT_THIRD_EXCHANGE_CANCEL = "merchant_third_exchange_cancel";
    public static final String MERCHANT_THIRD_TTL_QUEUE_CANCEL = "merchant_third_ttl_queue_cancel";
    public static final String MERCHANT_THIRD_TTL_EXCHANGE_CANCEL = "merchant_third_ttl_exchange_cancel";

    /**
     * 訂單消息超時後所綁定的交換機
     */
    @Bean
    DirectExchange orderDirectThird() {
        return ExchangeBuilder
                .directExchange(MERCHANT_THIRD_EXCHANGE_CANCEL)
                .durable(true)
                .build();
    }

    /**
     * 訂單延遲隊列隊列所綁定的交換機
     */
    @Bean
    DirectExchange orderTtlDirectThird() {
        return ExchangeBuilder
                .directExchange(MERCHANT_THIRD_TTL_EXCHANGE_CANCEL)
                .durable(true)
                .build();
    }

    /**
     * 超時訂單實際消費隊列
     */
    @Bean
    public Queue orderQueueThird() {
        return new Queue(MERCHANT_THIRD_QUEUE_CANCEL);
    }

    /**
     * 訂單延遲隊列（死信隊列）
     */
    @Bean
    public Queue orderTtlQueueThird() {
        return QueueBuilder
                .durable(MERCHANT_THIRD_TTL_QUEUE_CANCEL)
                .withArgument("x-dead-letter-exchange", MERCHANT_THIRD_EXCHANGE_CANCEL)//到期後轉發的交換機
                .withArgument("x-dead-letter-routing-key", MERCHANT_THIRD_QUEUE_CANCEL)//到期後轉發的路由鍵
                .build();
    }

    /**
     * 將超時訂單消費隊列綁定到超時交換機
     */
    @Bean
    Binding orderBindingThird() {
        return BindingBuilder
                .bind(orderQueueThird())
                .to(orderDirectThird())
                .with(MERCHANT_THIRD_QUEUE_CANCEL);
    }

    /**
     * 將訂單延遲隊列綁定到延遲交換機
     */
    @Bean
    Binding orderTtlBindingThird() {
        return BindingBuilder
                .bind(orderTtlQueueThird())
                .to(orderTtlDirectThird())
                .with(MERCHANT_THIRD_TTL_QUEUE_CANCEL);
    }

}


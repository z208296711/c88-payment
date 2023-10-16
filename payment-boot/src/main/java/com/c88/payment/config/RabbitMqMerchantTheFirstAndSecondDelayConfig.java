package com.c88.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqMerchantTheFirstAndSecondDelayConfig {

    public static final String MERCHANT_FIRST_SECOND_QUEUE_CANCEL = "merchant_first_second_queue_cancel";
    public static final String MERCHANT_FIRST_SECOND_EXCHANGE_CANCEL = "merchant_first_second_exchange_cancel";
    public static final String MERCHANT_FIRST_SECOND_TTL_QUEUE_CANCEL = "merchant_first_second_ttl_queue_cancel";
    public static final String MERCHANT_FIRST_SECOND_TTL_EXCHANGE_CANCEL = "merchant_first_second_ttl_exchange_cancel";

    /**
     * 訂單消息超時後所綁定的交換機
     */
    @Bean
    DirectExchange orderDirectFirstAndSecond() {
        return ExchangeBuilder
                .directExchange(MERCHANT_FIRST_SECOND_EXCHANGE_CANCEL)
                .durable(true)
                .build();
    }

    /**
     * 訂單延遲隊列隊列所綁定的交換機
     */
    @Bean
    DirectExchange orderTtlDirectFirstAndSecond() {
        return ExchangeBuilder
                .directExchange(MERCHANT_FIRST_SECOND_TTL_EXCHANGE_CANCEL)
                .durable(true)
                .build();
    }

    /**
     * 超時訂單實際消費隊列
     */
    @Bean
    public Queue orderQueueFirstAndSecond() {
        return new Queue(MERCHANT_FIRST_SECOND_QUEUE_CANCEL);
    }

    /**
     * 訂單延遲隊列（死信隊列）
     */
    @Bean
    public Queue orderTtlQueueFirstAndSecond() {
        return QueueBuilder
                .durable(MERCHANT_FIRST_SECOND_TTL_QUEUE_CANCEL)
                .withArgument("x-dead-letter-exchange", MERCHANT_FIRST_SECOND_EXCHANGE_CANCEL)//到期後轉發的交換機
                .withArgument("x-dead-letter-routing-key", MERCHANT_FIRST_SECOND_QUEUE_CANCEL)//到期後轉發的路由鍵
                .build();
    }

    /**
     * 將超時訂單消費隊列綁定到超時交換機
     */
    @Bean
    Binding orderBindingFirstAndSecond() {
        return BindingBuilder
                .bind(orderQueueFirstAndSecond())
                .to(orderDirectFirstAndSecond())
                .with(MERCHANT_FIRST_SECOND_QUEUE_CANCEL);
    }

    /**
     * 將訂單延遲隊列綁定到延遲交換機
     */
    @Bean
    Binding orderTtlBindingFirstAndSecond() {
        return BindingBuilder
                .bind(orderTtlQueueFirstAndSecond())
                .to(orderTtlDirectFirstAndSecond())
                .with(MERCHANT_FIRST_SECOND_TTL_QUEUE_CANCEL);
    }

}


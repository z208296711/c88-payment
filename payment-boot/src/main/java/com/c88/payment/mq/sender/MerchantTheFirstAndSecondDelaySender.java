package com.c88.payment.mq.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.c88.payment.config.RabbitMqMerchantTheFirstAndSecondDelayConfig.MERCHANT_FIRST_SECOND_TTL_EXCHANGE_CANCEL;
import static com.c88.payment.config.RabbitMqMerchantTheFirstAndSecondDelayConfig.MERCHANT_FIRST_SECOND_TTL_QUEUE_CANCEL;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantTheFirstAndSecondDelaySender {

    private final AmqpTemplate amqpTemplate;

    private final RedisTemplate<String, String> redisTemplate;

    // 延时时间10分
    private static final String DEFAULT_DELAY_TIME = "600000";

    private static final String KEY = "firstAndSecondDelaySenderDefaultMillisecond";

    public void sendMessage(Long memberRechargeId) {
        // 測試用，使用時要先清除queue
        String delayTime = DEFAULT_DELAY_TIME;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(KEY))) {
            delayTime = redisTemplate.opsForValue().get(KEY);
        } else {
            redisTemplate.opsForValue().set(KEY, DEFAULT_DELAY_TIME);
        }

        String finalDelayTime = delayTime;
        amqpTemplate.convertAndSend(MERCHANT_FIRST_SECOND_TTL_EXCHANGE_CANCEL, MERCHANT_FIRST_SECOND_TTL_QUEUE_CANCEL, memberRechargeId.toString(), message -> {
                    message.getMessageProperties()
                            .setExpiration(finalDelayTime);
                    return message;
                }
        );
        log.info("send delay message orderId:{}", memberRechargeId);
    }

}

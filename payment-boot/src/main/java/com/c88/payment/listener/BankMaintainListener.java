package com.c88.payment.listener;

import com.c88.payment.constants.RedisKey;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.service.IBankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 監聽金流RedisKey
 */
@Slf4j
@Component
public class BankMaintainListener extends KeyExpirationEventMessageListener {

    @Resource
    private IBankService iBankService;

    public BankMaintainListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
        setKeyspaceNotificationsConfigParameter("");
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        String[] redisKeyGen = expiredKey.split(":");

        //修改銀行維護狀態
        if (RedisKey.BANK_MAINTAIN.equals(redisKeyGen[0])) {
            log.info("modify start time:{}", expiredKey);
            Bank bank = iBankService.getById(redisKeyGen[1]);
            iBankService.lambdaUpdate().eq(Bank::getId, redisKeyGen[1])
                    .set(Bank::getState, iBankService.getBankState(bank).getValue())
                    .update();
        }
    }

}

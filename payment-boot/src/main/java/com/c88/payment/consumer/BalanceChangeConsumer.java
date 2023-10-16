package com.c88.payment.consumer;

import com.alibaba.fastjson.JSON;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.service.IMemberBalanceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.annotation.Transactional;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;

@Slf4j
@Configuration
@AllArgsConstructor
public class BalanceChangeConsumer {

    private final IMemberBalanceService memberBalanceService;

    @KafkaListener(id = BALANCE_CHANGE, topics = BALANCE_CHANGE)
    @Transactional
    public void listenBalanceChange(AddBalanceDTO addBalanceDTO, Acknowledgment acknowledgement) {
        log.info("listenBalanceChange Consumer: {}", JSON.toJSONString(addBalanceDTO));
        try {
            memberBalanceService.addBalance(addBalanceDTO);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("listenBalanceChange dto:[{}] error", JSON.toJSONString(addBalanceDTO), e.getMessage());
        } finally {
            acknowledgement.acknowledge();
        }
    }
}

package com.c88.payment.consumer;

import com.alibaba.fastjson.JSON;
import com.c88.payment.dto.AddAffiliateBalanceDTO;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.service.IAffiliateBalanceService;
import com.c88.payment.service.IMemberBalanceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.annotation.Transactional;

import static com.c88.common.core.constant.TopicConstants.AFFILIATE_BALANCE_CHANGE;
import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;

@Slf4j
@Configuration
@AllArgsConstructor
public class AffiliateBalanceChangeConsumer {

    private final IAffiliateBalanceService iAffiliateBalanceService;

    @KafkaListener(id = AFFILIATE_BALANCE_CHANGE, topics = AFFILIATE_BALANCE_CHANGE)
    @Transactional
    public void listenAffiliateBalanceChange(AddAffiliateBalanceDTO addBalanceDTO, Acknowledgment acknowledgement) {
        log.info("listenAffiliateBalanceChange Consumer: {}", JSON.toJSONString(addBalanceDTO));
        try {
            iAffiliateBalanceService.addBalance(addBalanceDTO);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("listenAffiliateBalanceChange dto:[{}] error", JSON.toJSONString(addBalanceDTO), e.getMessage());
        } finally {
            acknowledgement.acknowledge();
        }
    }
}

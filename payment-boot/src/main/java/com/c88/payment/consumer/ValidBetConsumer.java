package com.c88.payment.consumer;

import com.alibaba.fastjson.JSON;
import com.c88.common.core.constant.TopicConstants;
import com.c88.game.adapter.enums.BetOrderEventTypeEnum;
import com.c88.game.adapter.event.BetRecord;
import com.c88.payment.pojo.entity.MemberWithdrawBet;
import com.c88.payment.service.IMemberWithdrawBetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.c88.common.core.constant.TopicConstants.VALID_BET;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidBetConsumer {

    private final IMemberWithdrawBetService iMemberWithdrawBetService;

    // @KafkaListener(id = VALID_BET, topics = TopicConstants.VALID_BET, containerFactory = "validBetKafkaListenerContainerFactory")
    @KafkaListener(id = VALID_BET, topics = TopicConstants.VALID_BET)
    public void listenValidBet(BetRecord betRecord, Acknowledgment acknowledgement) {
        log.info("listenValidBet Consumer: {}", JSON.toJSONString(betRecord));
        try {
            MemberWithdrawBet memberWithdrawBet = iMemberWithdrawBetService.findMemberWithdrawBet(betRecord.getMemberId(), betRecord.getUsername());
            if (Objects.equals(betRecord.getEventType(), BetOrderEventTypeEnum.BET_SETTLED.getValue())) {
                //注單成立扣除提款流水
                memberWithdrawBet.setValidBet(memberWithdrawBet.getValidBet().add(betRecord.getValidBetAmount()));
            } else if (Objects.equals(betRecord.getEventType(), BetOrderEventTypeEnum.BET_CANCELED.getValue())) {
                //注單被取消必須扣回來
                memberWithdrawBet.setValidBet(memberWithdrawBet.getValidBet().subtract(betRecord.getValidBetAmount()));
            }
            iMemberWithdrawBetService.updateById(memberWithdrawBet);
        } catch (Exception e) {
            log.error("listenValidBet dto:[{}] error", JSON.toJSONString(betRecord), e.getMessage());
        } finally {
            acknowledgement.acknowledge();
        }
    }
}

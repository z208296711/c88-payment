package com.c88.payment.mq.receiver;

import com.c88.common.core.result.Result;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.enums.PayStatusEnum;
import com.c88.payment.enums.PrinceOrderStatus;
import com.c88.payment.mq.sender.MerchantTheFirstAndSecondDelaySender;
import com.c88.payment.mq.sender.MerchantTheThirdDelaySender;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.vo.CheckThirdPartyPaymentVO;
import com.c88.payment.service.IMemberRechargeService;
import com.c88.payment.service.IMerchantService;
import com.c88.payment.service.thirdparty.IThirdPartPayService;
import com.c88.payment.service.thirdparty.ThirdPartPaymentExecutor;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.RECHARGE;
import static com.c88.payment.config.RabbitMqMerchantTheFirstAndSecondDelayConfig.MERCHANT_FIRST_SECOND_QUEUE_CANCEL;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantTheFirstAndSecondDelayReceiver {

    private final IMerchantService iMerchantService;

    private final IMemberRechargeService iMemberRechargeService;

    private final ThirdPartPaymentExecutor thirdPartPaymentExecutor;

    private final MerchantTheFirstAndSecondDelaySender merchantFirstAndSecondFailSender;

    private final MerchantTheThirdDelaySender merchantTheThirdDelaySender;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 最後檢查次數
    private static final Integer LAST_FIND_BACK = 2;

    @RabbitListener(queues = MERCHANT_FIRST_SECOND_QUEUE_CANCEL, containerFactory = "manualAckRabbitListenerContainerFactory")
    public void receive(Message message, Channel channel, @Payload String memberRechargeId, @Headers Map<String, Object> headers) throws IOException {
        log.info("MerchantTheFirstAndSecondDelayReceiver: " + LocalDateTime.now() + "  Received: " + message);

        try {
            Optional<MemberRecharge> memberRechargeOPT = iMemberRechargeService.lambdaQuery()
                    .eq(MemberRecharge::getId, memberRechargeId)
                    .eq(MemberRecharge::getStatus, PayStatusEnum.IN_PROGRESS.getValue())
                    .le(MemberRecharge::getFindBack, LAST_FIND_BACK)
                    .oneOpt();

            // 找不到資料時不處理，最多查找三次
            memberRechargeOPT.ifPresent(memberRecharge -> {
                        // 取得商戶
                        Merchant merchant = iMerchantService.getMerchantById(memberRecharge.getMerchantId());

                        // 取得對應的三方設定
                        IThirdPartPayService iThirdPartPayService = thirdPartPaymentExecutor.findByMerchantCode(merchant.getCode());

                        // 取得訂單狀態
                        Result<CheckThirdPartyPaymentVO> checkThirdPartyPaymentVOResult = iThirdPartPayService.checkPaymentStatus(merchant, memberRecharge.getTradeNo());
                        if (!Result.isSuccess(checkThirdPartyPaymentVOResult)) {
                            return;
                        }

                        // 判斷如果交易成功則寫入成功否則繼續到下個時間檢查
                        CheckThirdPartyPaymentVO checkThirdPartyPaymentVO = checkThirdPartyPaymentVOResult.getData();
                        if (Objects.equals(checkThirdPartyPaymentVO.getStatus(), PrinceOrderStatus.SUCCESS.getCode())) {

                            BigDecimal amount = memberRecharge.getAmount().subtract(memberRecharge.getFee());

                            // 訂單成立用戶增加餘額及提領限額
                            kafkaTemplate.send(BALANCE_CHANGE,
                                    AddBalanceDTO.builder()
                                            .memberId(memberRecharge.getMemberId())
                                            .balance(amount)
                                            .balanceChangeTypeLinkEnum(RECHARGE)
                                            .type(RECHARGE.getType())
                                            .betRate(BigDecimal.ONE)
                                            .note(RECHARGE.getI18n())
                                            .build()
                            );

                            iMemberRechargeService.lambdaUpdate()
                                    .eq(MemberRecharge::getId, memberRecharge.getId())
                                    .set(MemberRecharge::getStatus, PayStatusEnum.SUCCESS.getValue())
                                    .set(MemberRecharge::getOutTradeNo, checkThirdPartyPaymentVO.getTransactionId())
                                    .set(MemberRecharge::getRealAmount, amount)
                                    .set(MemberRecharge::getSuccessTime, LocalDateTime.now())
                                    .update();
                            return;
                        }

                        // 進入下個循環
                        Integer findBack = memberRecharge.getFindBack();

                        iMemberRechargeService.lambdaUpdate()
                                .eq(MemberRecharge::getId, memberRecharge.getId())
                                .set(MemberRecharge::getFindBack, ++findBack)
                                .update();

                        if (findBack < LAST_FIND_BACK) {
                            merchantFirstAndSecondFailSender.sendMessage(memberRecharge.getId());
                        } else {
                            merchantTheThirdDelaySender.sendMessage(memberRecharge.getId());
                        }
                    }
            );
        } catch (Exception e) {
            log.info("MerchantTheFirstAndSecondDelayReceiver: {}", ExceptionUtils.getFullStackTrace(e));
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

}

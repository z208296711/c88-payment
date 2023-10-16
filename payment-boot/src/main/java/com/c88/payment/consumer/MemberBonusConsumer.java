package com.c88.payment.consumer;

import com.alibaba.fastjson.JSON;
import com.c88.affiliate.api.dto.AffiliateMemberDTO;
import com.c88.affiliate.api.feign.AffiliateMemberClient;
import com.c88.common.core.constant.TopicConstants;
import com.c88.common.core.enums.BalanceChangeTypeLinkEnum;
import com.c88.common.core.result.Result;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.payment.dto.RechargeBonusDTO;
import com.c88.payment.pojo.entity.MemberBonusRecord;
import com.c88.payment.service.IMemberBonusRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberBonusConsumer {

    private static final String GROUP_ID = "MEMBER_BONUS";

    private final IMemberBonusRecordService iMemberBonusRecordService;

    private final MemberFeignClient memberFeignClient;

    private final AffiliateMemberClient affiliateMemberClient;

    @KafkaListener(id = GROUP_ID, topics = TopicConstants.RECHARGE_BONUS)
    public void listenMemberRechargeBonus(RechargeBonusDTO rechargeBonusDTO, Acknowledgment acknowledgment) {

        log.info("listenMemberRechargeBonus Consumer: {}", JSON.toJSONString(rechargeBonusDTO));
        try {
            BalanceChangeTypeLinkEnum balanceChangeTypeLinkEnum = rechargeBonusDTO.getBalanceChangeTypeLinkEnum();

            Result<MemberInfoDTO> memberInfoResult = memberFeignClient.getMemberInfo(rechargeBonusDTO.getMemberId());
            MemberInfoDTO memberInfo = MemberInfoDTO.builder().build();
            if (Result.isSuccess(memberInfoResult)) {
                memberInfo = memberInfoResult.getData();
            }

            Result<List<AffiliateMemberDTO>> affiliateMembersResult = affiliateMemberClient.findAffiliateMembers(List.of(rechargeBonusDTO.getMemberId()));
            AffiliateMemberDTO affiliateMember = new AffiliateMemberDTO();
            if (Result.isSuccess(affiliateMembersResult)) {
                List<AffiliateMemberDTO> affiliateMembers = affiliateMembersResult.getData();
                affiliateMember = affiliateMembers.stream()
                        .findFirst()
                        .orElse(new AffiliateMemberDTO());
            }

            iMemberBonusRecordService.save(
                    MemberBonusRecord.builder()
                            .memberId(rechargeBonusDTO.getMemberId())
                            .username(rechargeBonusDTO.getUsername())
                            .parentUsername(affiliateMember.getParentUsername())
                            .name(balanceChangeTypeLinkEnum.getI18n())
                            .amount(rechargeBonusDTO.getAmount())
                            .betRate(rechargeBonusDTO.getBetRate())
                            .bet(rechargeBonusDTO.getAmount().multiply(rechargeBonusDTO.getBetRate()))
                            .type(balanceChangeTypeLinkEnum.getCode())
                            .note(rechargeBonusDTO.getNote())
                            .reviewUsername(rechargeBonusDTO.getReviewUsername())
                            .receiveVipLevelName(memberInfo.getCurrentVipName())
                            .receiveTime(rechargeBonusDTO.getGmtCreate())
                            .build()
            );

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("listenMemberRechargeBonus dto:[{}] error:{}", JSON.toJSONString(rechargeBonusDTO), e.getMessage());
        }
    }
}

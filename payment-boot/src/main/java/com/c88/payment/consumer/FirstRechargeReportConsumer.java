package com.c88.payment.consumer;

import com.c88.affiliate.api.dto.AffiliateMemberDTO;
import com.c88.affiliate.api.feign.AffiliateMemberClient;
import com.c88.common.core.result.Result;
import com.c88.payment.dto.MemberFirstRechargeDTO;
import com.c88.payment.pojo.entity.MemberFirstRechargeReport;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.entity.RechargeType;
import com.c88.payment.service.IMemberFirstRechargeReportService;
import com.c88.payment.service.IMemberRechargeService;
import com.c88.payment.service.IRechargeTypeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.c88.common.core.constant.TopicConstants.MEMBER_FIRST_RECHARGE;

@Slf4j
@Configuration
@AllArgsConstructor
public class FirstRechargeReportConsumer {

    private static final String MEMBER_FIRST_RECHARGE_REPORT = "member_first_recharge_report";

    private final IMemberRechargeService iMemberRechargeService;

    private final IRechargeTypeService iRechargeTypeService;

    private final IMemberFirstRechargeReportService iMemberFirstRechargeReportService;

    private final AffiliateMemberClient affiliateMemberClient;

    @Transactional
    @KafkaListener(id = MEMBER_FIRST_RECHARGE_REPORT, topics = MEMBER_FIRST_RECHARGE)
    public void listenFirstRechargeReport(MemberFirstRechargeDTO memberFirstRecharge, Acknowledgment acknowledgement) {
        log.info("FirstRechargeReport Consumer: {}", memberFirstRecharge);
        try {
            MemberRecharge memberRecharge = iMemberRechargeService.lambdaQuery()
                    .eq(MemberRecharge::getTradeNo, memberFirstRecharge.getOrderId())
                    .oneOpt()
                    .orElse(MemberRecharge.builder().build());

            RechargeType rechargeType = iRechargeTypeService.getById(memberRecharge.getRechargeTypeId());

            Result<List<AffiliateMemberDTO>> affiliateMembersResult = affiliateMemberClient.findAffiliateMembers(List.of(memberRecharge.getMemberId()));
            AffiliateMemberDTO affiliateMemberDTO = new AffiliateMemberDTO();
            if (Result.isSuccess(affiliateMembersResult)) {
                affiliateMemberDTO = affiliateMembersResult.getData().stream().findFirst().orElse(new AffiliateMemberDTO());
            }

            iMemberFirstRechargeReportService.save(
                    MemberFirstRechargeReport.builder()
                            .rechargeTime(memberRecharge.getCreateTime())
                            .orderNo(memberFirstRecharge.getOrderId())
                            .username(memberRecharge.getUsername())
                            .parentUsername(affiliateMemberDTO.getParentUsername())
                            .realName(StringUtils.isNotBlank(memberRecharge.getRealName()) ? memberRecharge.getRealName() : "")
                            .firstRechargeAmount(memberRecharge.getAmount())
                            .rechargeType(rechargeType.getName())
                            .realTime(memberRecharge.getSuccessTime())
                            .build()
            );

            acknowledgement.acknowledge();
        } catch (Exception e) {
            log.error("FirstRechargeReport dto:[{}] error", memberFirstRecharge, e.getMessage());
        }
    }
}

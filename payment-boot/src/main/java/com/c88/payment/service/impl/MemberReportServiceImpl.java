package com.c88.payment.service.impl;

import cn.hutool.core.stream.CollectorUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.affiliate.api.dto.AffiliateMemberDTO;
import com.c88.affiliate.api.feign.AffiliateFeignClient;
import com.c88.affiliate.api.feign.AffiliateMemberClient;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.mybatis.util.PageUtil;
import com.c88.common.web.exception.BizException;
import com.c88.game.adapter.api.GameFeignClient;
import com.c88.game.adapter.dto.CategoryRebateRecordDTO;
import com.c88.game.adapter.dto.MemberRebateRecordDTO;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.payment.mapper.MemberBalanceMapper;
import com.c88.payment.mapper.MemberRechargeMapper;
import com.c88.payment.pojo.entity.MemberBalance;
import com.c88.payment.pojo.form.SearchMemberWinLossForm;
import com.c88.payment.pojo.vo.MemberWinLossVO;
import com.c88.payment.service.IMemberReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description: TODO
 * @author: marcoyang
 * @date: 2022/12/29
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberReportServiceImpl implements IMemberReportService {

    private final MemberRechargeMapper memberRechargeMapper;

    private final GameFeignClient gameFeignClient;

    private final AffiliateMemberClient affiliateMemberClient;

    private final MemberBalanceMapper memberBalanceMapper;

    private final MemberFeignClient memberFeignClient;

    @Override
    public IPage<MemberWinLossVO> getMemberWinLoss(SearchMemberWinLossForm form) {
        Result<MemberInfoDTO> result = memberFeignClient.getMemberInfo(form.getUsername());
        MemberInfoDTO dto = Result.isSuccess(result) ? result.getData() : null;
        Long memberId = Optional.ofNullable(dto).map(MemberInfoDTO::getId).orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        List<MemberWinLossVO> memberWinLossX = memberRechargeMapper.findMemberWinLossByDate(form);

        List<Long> members = List.of(memberId);
        Result<List<Map<String,Object>>> betSumResult = gameFeignClient.getBetSumByMemberIds(members,form.getStartTime().toString(),form.getEndTime().toString());
        List<Map<String,Object>> betSums = Result.isSuccess(betSumResult) ? betSumResult.getData() : List.of();

        Result<List<AffiliateMemberDTO>> affiliateMemberX = affiliateMemberClient.findAffiliateMembers(members);
        List<AffiliateMemberDTO> affiliateX = Result.isSuccess(affiliateMemberX) ? affiliateMemberX.getData() : List.of();
        Map<Long,AffiliateMemberDTO> affiliateXMap = affiliateX.stream().collect(Collectors.toMap(AffiliateMemberDTO::getMemberId,x->x));

        List<MemberBalance> balanceX = memberBalanceMapper.selectList(Wrappers.<MemberBalance>query()
                .lambda().in(MemberBalance::getMemberId,members));

        Map<Long,MemberBalance> balanceMap = balanceX.stream().collect(Collectors.toMap(MemberBalance::getMemberId,x->x));

        Map<String,Map<String,Object>> mapBetSum = betSums.stream().collect(Collectors.toMap(y->(String)y.get("createDate")+y.get("memberId"),x->x));

        memberWinLossX = memberWinLossX.stream().map(x -> toMemberWinLossVO(x,mapBetSum,affiliateXMap,balanceMap)).collect(Collectors.toList());
        List<MemberWinLossVO> nonPaymentX = toMemberWinLossVOFromMap(mapBetSum, affiliateXMap, balanceMap);
        memberWinLossX.addAll(nonPaymentX);
        List<MemberWinLossVO> ls = memberWinLossX.stream().sorted( Comparator.comparing(MemberWinLossVO::getCreateDate).reversed()).collect(Collectors.toList());
        Map<LocalDate, Map<Long, List<MemberRebateRecordDTO>>> rebateMap = gameFeignClient.findRebateRecordMember(form.getStartTime().toString(), form.getEndTime().toString()).getData()
                .stream().collect(Collectors.groupingBy(MemberRebateRecordDTO::getGmtCreate
                        , Collectors.groupingBy(MemberRebateRecordDTO::getMemberId)));

        for (MemberWinLossVO l : ls) {
            l.setRebate(BigDecimal.ZERO);
            if(rebateMap.containsKey(l.getCreateDate())){
                List<MemberRebateRecordDTO> categoryRebateRecordDTOS = rebateMap.get(l.getCreateDate()).get((long) l.getMemberId());
                if(CollectionUtils.isNotEmpty(categoryRebateRecordDTOS)){
                    l.setRebate(categoryRebateRecordDTOS.stream().map(MemberRebateRecordDTO::getRebate).reduce(BigDecimal.ZERO, BigDecimal::add));
                }
            }
        }
        return PageUtil.toIPage( ls, form.getPageNum(), form.getPageSize());
    }



    private MemberWinLossVO toMemberWinLossVO(MemberWinLossVO vo, Map<String,Map<String,Object>> mapBetSum, Map<Long,AffiliateMemberDTO> affiliateXMap, Map<Long,MemberBalance> balanceMap){
        String key = vo.getCreateDate().toString() + vo.getMemberId();
        Map<String,Object> betSum = mapBetSum.get(key);
        AffiliateMemberDTO dto= affiliateXMap.get((long)vo.getMemberId());
        MemberBalance balance = balanceMap.get((long)vo.getMemberId());
        vo.setAgentName(dto.getParentUsername());
        vo.setUsername(dto.getMemberUsername());
        vo.setDownPaymentAmount(balance.getFirstRechargeBalance());
        BigDecimal betAmount =  Optional.ofNullable(betSum).map(x -> BigDecimal.valueOf((double)x.get("betAmount"))).orElse(BigDecimal.ZERO);
        BigDecimal winLoss = Optional.ofNullable(betSum).map(x -> BigDecimal.valueOf((double)x.get("winLossAmount"))).orElse(BigDecimal.ZERO);
        log.info("memberId {} winLoss : {} award {} bonus {} fee {} bet {} ",vo.getMemberId(), winLoss, vo.getDepositAward(),vo.getBonus(),vo.getFee(),betAmount);
        BigDecimal netProfit = winLoss.multiply(BigDecimal.valueOf(-1L)).subtract(vo.getDepositAward()).subtract(vo.getBonus()).subtract(vo.getFee());
        vo.setBetAmount(betAmount);
        vo.setNetProfit(netProfit);
        mapBetSum.remove(key);
        return vo;
    }

    private List<MemberWinLossVO> toMemberWinLossVOFromMap(Map<String,Map<String,Object>> mapBetSum , Map<Long,AffiliateMemberDTO> affiliateXMap, Map<Long,MemberBalance> balanceMap){
        Long memberId = 0L;
        if (mapBetSum.size() > 0) {
            String key = mapBetSum.keySet().stream().findFirst().get();
            memberId = (long) (Integer) mapBetSum.get(key).get("memberId");
        }else{
            return List.of();
        }
        AffiliateMemberDTO dto= affiliateXMap.get(memberId);
        MemberBalance balance = balanceMap.get(memberId);
        return mapBetSum.entrySet().stream().map(x-> {
            BigDecimal betAmount = BigDecimal.valueOf((double)x.getValue().get("betAmount"));
            BigDecimal winLossAmount = BigDecimal.valueOf((double)x.getValue().get("winLossAmount"));
            return MemberWinLossVO.builder()
                    .username(balance.getUsername())
                    .memberId((Integer)x.getValue().get("memberId"))
                    .createDate(LocalDate.parse((String)x.getValue().get("createDate")))
                    .agentName(dto.getParentUsername())
                    .downPaymentAmount(balance.getFirstRechargeBalance())
                    .betAmount(betAmount)
                    .netProfit(winLossAmount.multiply(BigDecimal.valueOf(-1L)))
                    .depositAmount(BigDecimal.ZERO)
                    .fee(BigDecimal.ZERO)
                    .bonus(BigDecimal.ZERO).depositAmount(BigDecimal.ZERO)
                    .depositAward(BigDecimal.ZERO).depositRowNum(0).withdrawAmount(BigDecimal.ZERO)
                    .withdrawRowNum(0).build();
        }).collect(Collectors.toList());
    }
}

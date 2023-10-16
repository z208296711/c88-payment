package com.c88.payment.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.api.GameFeignClient;
import com.c88.game.adapter.dto.CategoryRebateRecordDTO;
import com.c88.game.adapter.dto.MemberRebateRecordTotalDTO;
import com.c88.game.adapter.vo.ReportBetOrderVO;
import com.c88.member.api.MemberFeignClient;
import com.c88.payment.mapstruct.MemberBonusRecordConverter;
import com.c88.payment.mapstruct.MemberFirstRechargeReportConverter;
import com.c88.payment.pojo.entity.MemberBonusRecord;
import com.c88.payment.pojo.entity.MemberFirstRechargeReport;
import com.c88.payment.pojo.form.FindFirstRechargeMessageForm;
import com.c88.payment.pojo.form.FindMemberBonusRecordForm;
import com.c88.payment.pojo.form.RechargeWithdrawReportForm;
import com.c88.payment.pojo.form.SearchMemberWinLossForm;
import com.c88.payment.pojo.vo.CompanyReportVO;
import com.c88.payment.pojo.vo.FirstRechargeMessageTotalVO;
import com.c88.payment.pojo.vo.MemberBonusRecordTotalVO;
import com.c88.payment.pojo.vo.MemberBonusRecordVO;
import com.c88.payment.pojo.vo.MemberFirstRechargeReportVO;
import com.c88.payment.pojo.vo.MemberWinLossVO;
import com.c88.payment.pojo.vo.PageSummaryVO;
import com.c88.payment.pojo.vo.RechargeWithdrawAwardCountVO;
import com.c88.payment.pojo.vo.RechargeWithdrawReportVO;
import com.c88.payment.service.IMemberBalanceService;
import com.c88.payment.service.IMemberBonusRecordService;
import com.c88.payment.service.IMemberFirstRechargeReportService;
import com.c88.payment.service.IMemberRechargeService;
import com.c88.payment.service.IMemberReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Tag(name = "報表")
@RestController
@RequestMapping("/api/v1/report")
@Validated
@AllArgsConstructor
@Slf4j
public class ReportController {

    private final IMemberBalanceService memberBalanceService;

    private final IMemberRechargeService iMemberRechargeService;

    private final MemberFeignClient memberFeignClient;

    private final GameFeignClient gameFeignClient;

    private final IMemberFirstRechargeReportService iMemberFirstRechargeReportService;

    private final MemberFirstRechargeReportConverter memberFirstRechargeReportConverter;

    private final IMemberReportService iMemberReportService;

    private final IMemberBonusRecordService iMemberBonusRecordService;

    private final MemberBonusRecordConverter memberBonusRecordConverter;


    @Operation(summary = "玩家充提報表")
    @GetMapping("/recharge/withdraw")
    public PageResult<RechargeWithdrawReportVO> getRechargeWithdrawReport(@ParameterObject RechargeWithdrawReportForm form) {
        PageSummaryVO pageSummaryVO = memberBalanceService.getRechargeWithdrawReport(form);
        return PageResult.success(pageSummaryVO.getPage(), pageSummaryVO.getObject());
    }

    @Operation(summary = "首存信息")
    @GetMapping("/first/recharge/message")
    public PageResult<MemberFirstRechargeReportVO> findFirstRechargeMessage(@ParameterObject FindFirstRechargeMessageForm form) {
        IPage<MemberFirstRechargeReportVO> reportPage = iMemberFirstRechargeReportService.lambdaQuery()
                .ge(Objects.nonNull(form.getStartTime()), MemberFirstRechargeReport::getGmtCreate, form.getStartTime())
                .le(Objects.nonNull(form.getEndTime()), MemberFirstRechargeReport::getGmtCreate, form.getEndTime())
                .eq(StringUtils.isNotBlank(form.getUsername()), MemberFirstRechargeReport::getUsername, form.getUsername())
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(memberFirstRechargeReportConverter::toVO);

        // 取得首存總訊息(id = 人數)
        MemberFirstRechargeReport total = iMemberFirstRechargeReportService.getBaseMapper()
                .selectOne(Wrappers.<MemberFirstRechargeReport>query()
                        .select("count(1) as id",
                                "sum(first_recharge_amount) as firstRechargeAmount"
                        ).lambda()
                        .ge(Objects.nonNull(form.getStartTime()), MemberFirstRechargeReport::getGmtCreate, form.getStartTime())
                        .le(Objects.nonNull(form.getEndTime()), MemberFirstRechargeReport::getGmtCreate, form.getEndTime())
                        .eq(StringUtils.isNotBlank(form.getUsername()), MemberFirstRechargeReport::getUsername, form.getUsername())
                );

        return PageResult.success(reportPage, List.of(FirstRechargeMessageTotalVO.builder().firstRechargePersonNumber(total.getId()).firstRechargeAmount(total.getFirstRechargeAmount()).build()));
    }

    @Operation(summary = "公司輸贏報表")
    @GetMapping("/company")
    public Result<CompanyReportVO> getCompanyReport(@ParameterObject RechargeWithdrawReportForm form) {
        //新註冊會員
        Integer registerCount = memberFeignClient.getRegisterCount(form.getStartTime(), form.getEndTime()).getData();
        //首存
        CompanyReportVO firstRechargeCountAndAmount = memberBalanceService.getFirstRechargeCountAndAmount(form.getStartTime(), form.getEndTime());
        //存紅優人數及金額
        RechargeWithdrawAwardCountVO rechargeWithdrawAwardCountAndAmount = memberBalanceService.getRechargeAwardCountAndAmount(form.getStartTime(), form.getEndTime());
        //提款
        RechargeWithdrawAwardCountVO withdrawCountAndAmount = memberBalanceService.getWithdrawCountAndAmount(form.getStartTime(), form.getEndTime());
        //反水
        MemberRebateRecordTotalDTO memberRebateRecordTotalDTO = gameFeignClient.findRebateRecordSum(form.getStartTime(), form.getEndTime()).getData();
        //反水人數
        int rebateCount = Integer.parseInt(String.valueOf(gameFeignClient.findRebateRecordCategory(form.getStartTime(), form.getEndTime()).getData().stream().map(CategoryRebateRecordDTO::getMemberId).distinct().count()));


        //後台上分
        List<Integer> typeList = new ArrayList<>();
        typeList.add(2); //手動補單=後台上分
        typeList.add(3);
        List<RechargeWithdrawReportVO> successRechargeCount = iMemberRechargeService.getSuccessRechargeCount(1, typeList, form.getStartTime(), form.getEndTime());
        BigDecimal adminAmount = successRechargeCount.stream().map(RechargeWithdrawReportVO::getRechargeTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //投注人數金額
        ReportBetOrderVO betOrderCount = gameFeignClient.getBerOrderCount(form.getStartTime(), form.getEndTime()).getData();
        ReportBetOrderVO betOrderSettleCount = gameFeignClient.getBerOrderCountBySettleDate(form.getStartTime(), form.getEndTime()).getData();

        //存款並投注人數 計算
        List<Long> rechargeMembers = iMemberRechargeService.getRechargeMembers(form.getStartTime(), form.getEndTime());
        log.info("rechargeMembers:{}", rechargeMembers);
        List<Long> betMembers = betOrderCount.getBetMembers();
        log.info("betMembers:{}", betMembers);
        AtomicInteger rechargeBetMembers = new AtomicInteger();
        rechargeMembers.forEach(rm -> {
            if (betMembers.contains(rm)) rechargeBetMembers.getAndIncrement();
        });

        //手續費
        BigDecimal fee = iMemberRechargeService.getFee(form.getStartTime(), form.getEndTime());

//        //公司輸贏
        BigDecimal companyWinloss = betOrderCount.getAllBetAmount().subtract(betOrderSettleCount.getSettleAmount());
        return Result.success(CompanyReportVO.builder()
                .registerMemberCount(registerCount)
                .firstRechargeCount(firstRechargeCountAndAmount.getFirstRechargeCount())
                .firstRechargeAmount(firstRechargeCountAndAmount.getFirstRechargeAmount())
                .rechargeCount(rechargeWithdrawAwardCountAndAmount.getRechargeCount())
                .rechargeAmount(rechargeWithdrawAwardCountAndAmount.getRechargeAmount())
                .withdrawCount(withdrawCountAndAmount.getWithdrawCount())
                .withdrawAmount(withdrawCountAndAmount.getWithdrawAmount())
                .awardCount(rechargeWithdrawAwardCountAndAmount.getAwardCount())
                .awardAmount(rechargeWithdrawAwardCountAndAmount.getAwardAmount())
                .rechargeAwardCount(rechargeWithdrawAwardCountAndAmount.getRechargeAwardCount())
                .rechargeAwardAmount(rechargeWithdrawAwardCountAndAmount.getRechargeAwardAmount())
                .rebateCount(rebateCount)
                .rebateAmount(memberRebateRecordTotalDTO.getRebate())
                .promotionAmount(BigDecimal.ZERO)
                //TODO 這裡應該是取settleAmount，但結果會不正確，待驗證
//                .settleAmount(betOrderSettleCount.getSettleAmount())
                .settleAmount(betOrderSettleCount.getSettleAmount())
                .adminRechargeCount(successRechargeCount.size())
                .adminRechargeAmount(adminAmount)
                .betCount(betMembers.size())
                .rechargeAndBetCount(rechargeBetMembers.get())
                .allBetAmount(betOrderCount.getAllBetAmount())
                .allValidBetAmount(betOrderSettleCount.getAllValidBetAmount())
                .companyWinLoss(companyWinloss)
                //公司輸贏 - 返水金額 - 紅利金額 - 優惠金額 - 金流手續費 + 管理員扣除
                .companyProfit(companyWinloss
                        .subtract(rechargeWithdrawAwardCountAndAmount.getAwardAmount())
                        .subtract(rechargeWithdrawAwardCountAndAmount.getRechargeAwardAmount())
                        .subtract(fee)
                        .add(BigDecimal.ZERO))
                .diffAmount(rechargeWithdrawAwardCountAndAmount.getRechargeAmount().subtract(withdrawCountAndAmount.getWithdrawAmount()))
                .build());
    }

    @Operation(summary = "玩家輸贏報表", description = "查詢玩家輸贏報表")
    @GetMapping("/memberWinLoss")
    public PageResult<MemberWinLossVO> getMemberWinLossRecord(@Validated @ParameterObject SearchMemberWinLossForm form) {
        //iMemberReportService.getMemberWinLoss(form);
        return PageResult.success(iMemberReportService.getMemberWinLoss(form));
    }

    @Operation(summary = "找紅利列表")
    @GetMapping("/member/bonus/record")
    public PageResult<MemberBonusRecordVO> findMemberBonusRecord(@ParameterObject FindMemberBonusRecordForm form) {
        IPage<MemberBonusRecordVO> page = iMemberBonusRecordService.lambdaQuery()
                .eq(StringUtils.isNotBlank(form.getUsername()), MemberBonusRecord::getUsername, form.getUsername())
                .ge(Objects.nonNull(form.getStartTime()),MemberBonusRecord::getReceiveTime,form.getStartTime())
                .le(Objects.nonNull(form.getEndTime()),MemberBonusRecord::getReceiveTime,form.getEndTime())
                .orderByDesc(MemberBonusRecord::getReceiveTime)
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(memberBonusRecordConverter::toVO);

        MemberBonusRecord memberBonusRecord = Optional.ofNullable(iMemberBonusRecordService.getBaseMapper()
                        .selectOne(
                                Wrappers.<MemberBonusRecord>query()
                                        .select("sum(amount) as amount", "sum(bet) as bet")
                                        .lambda()
                                        .ge(Objects.nonNull(form.getStartTime()),MemberBonusRecord::getReceiveTime,form.getStartTime())
                                        .le(Objects.nonNull(form.getEndTime()),MemberBonusRecord::getReceiveTime,form.getEndTime())
                                        .eq(StringUtils.isNotBlank(form.getUsername()), MemberBonusRecord::getUsername, form.getUsername())
                        )
                )
                .orElse(MemberBonusRecord.builder().amount(BigDecimal.ZERO).bet(BigDecimal.ZERO).build());

        return PageResult.success(page,
                MemberBonusRecordTotalVO.builder()
                        .amount(memberBonusRecord.getAmount())
                        .bet(memberBonusRecord.getBet())
                        .build()
        );

    }

}

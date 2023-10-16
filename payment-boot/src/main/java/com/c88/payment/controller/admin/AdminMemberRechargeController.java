package com.c88.payment.controller.admin;

import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.log.OperationEnum;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.payment.enums.PayStatusEnum;
import com.c88.payment.form.SearchMemberDepositForm;
import com.c88.payment.mapstruct.MemberRechargeConverter;
import com.c88.payment.pojo.entity.MemberBalance;
import com.c88.payment.pojo.entity.MemberBonusRecord;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.RechargeModifyForm;
import com.c88.payment.pojo.vo.MemberCapitalSummary;
import com.c88.payment.service.IMemberBalanceService;
import com.c88.payment.service.IMemberBonusRecordService;
import com.c88.payment.service.IMemberRechargeService;
import com.c88.payment.service.IMemberWithdrawService;
import com.c88.payment.vo.MemberDepositDTO;
import com.c88.payment.vo.MemberRechargeDTO;
import com.c88.payment.vo.MemberTotalRechargeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Tag(name = "『後台』充值相關")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recharge")
public class AdminMemberRechargeController {

    private final IMemberRechargeService iMemberRechargeService;

    private final IMemberWithdrawService iMemberWithdrawService;

    private final IMemberBalanceService iMemberBalanceService;

    private final IMemberBonusRecordService iMemberBonusRecordService;

    private final MemberRechargeConverter memberRechargeConverter;

    private final MemberFeignClient memberFeignClient;

    @Operation(summary = "手動補單訂單修改狀態")
    @PatchMapping(value = "/status")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#form.tradeNo}",
            menu = "menu.finance",
            menuPage = "menu.manual-recharge",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "#form.status==1?'be_company_deposit.operation_log01':'be_company_deposit.operation_log02'")
    public Result<Void> updateRechargeStatus(@RequestBody RechargeModifyForm form) {
        return Result.judge(iMemberRechargeService.updateRecharge(form));
    }

    @GetMapping(value = "/total/{uid}")
    public Result<BigDecimal> memberTotalRecharge(@PathVariable Long uid) {
        return Result.success(iMemberRechargeService.memberTotalRecharge(uid, null));
    }

    @GetMapping(value = "/from/{uid}")
    public Result<BigDecimal> memberTotalRechargeFrom(@PathVariable Long uid,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime) {
        return Result.success(iMemberRechargeService.memberTotalRecharge(uid, fromTime));
    }

    @GetMapping(value = "/from/to/{uid}")
    public Result<BigDecimal> memberTotalRechargeFrom(@PathVariable Long uid,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime) {

        return Result.success(iMemberRechargeService.memberTotalRecharge(uid, fromTime, toTime));
    }

    @Operation(summary = "查詢區段時間各會員充值總額")
    @GetMapping("/member/total")
    public Result<List<MemberTotalRechargeVO>> findMemberTotalRecharge(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime) {
        return Result.success(iMemberRechargeService.findMemberTotalRecharge(fromTime, toTime));
    }

    @Operation(summary = "查詢會員 大於指定金額的充值單")
    @GetMapping("/member/{uid}/single/recharge")
    public Result<List<MemberRechargeDTO>> findMemberSingleRecharge(@PathVariable Long uid,
                                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime,
                                                                    @RequestParam BigDecimal amount) {
        return Result.success(memberRechargeConverter.toDTO(iMemberRechargeService.lambdaQuery().eq(MemberRecharge::getMemberId, uid)
                .eq(MemberRecharge::getStatus, PayStatusEnum.SUCCESS.getValue())
                .ge(MemberRecharge::getAmount, amount)
                .ge(fromTime != null, MemberRecharge::getSuccessTime, fromTime)
                .le(toTime != null, MemberRecharge::getSuccessTime, toTime)
                .list()));
    }

    @Operation(summary = "查詢會員充值單")
    @GetMapping("/member/{username}/recharge")
    public PageResult<MemberDepositDTO> findMemberRecharges(@PathVariable String username,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beginTime,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                            @RequestParam(required = false, defaultValue = "1") int pageNum,
                                                            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        SearchMemberDepositForm form = new SearchMemberDepositForm();
        form.setUsername(username);
        form.setBeginTime(beginTime);
        form.setEndTime(endTime);
        form.setPageNum(pageNum);
        form.setPageSize(pageSize);
        return PageResult.success(iMemberRechargeService.findMemberRechargeFromAffiliate(form));
    }

    @Operation(summary = "找會員資本概括")
    @GetMapping("/member/capital/summary/{id}")
    public Result<MemberCapitalSummary> findMemberCapitalSummary(@PathVariable("id") Long id) {
        Result<MemberInfoDTO> memberInfoResult = memberFeignClient.getMemberInfo(id);
        if(Result.isFail(memberInfoResult)){
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }

        // 取得充值訊息
        List<MemberRecharge> memberRecharges = iMemberRechargeService.lambdaQuery()
                .eq(MemberRecharge::getMemberId, id)
                .eq(MemberRecharge::getStatus, PayStatusEnum.SUCCESS.getValue())
                .list();

        // 取得提款訊息
        List<MemberWithdraw> memberWithdraws = iMemberWithdrawService.lambdaQuery()
                .eq(MemberWithdraw::getUid, id)
                .eq(MemberWithdraw::getType, 0)
                .in(MemberWithdraw::getRemitState, List.of(2, 4))
                .list();

        // 取得會員錢包訊息
        MemberBalance memberBalance = iMemberBalanceService.lambdaQuery()
                .eq(MemberBalance::getMemberId, id)
                .oneOpt()
                .orElse(MemberBalance.builder().build());

        // 取得會員紅利訊息
        List<MemberBonusRecord> memberBonusRecords = iMemberBonusRecordService.lambdaQuery()
                .eq(MemberBonusRecord::getMemberId, id)
                .list();

        // 今日充值
        BigDecimal todayRechargeAmount = memberRecharges.stream()
                .filter(filter ->
                        filter.getSuccessTime().isAfter(LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                                && filter.getSuccessTime().isBefore(LocalDateTime.of(LocalDate.now(), LocalTime.MAX)))
                .map(MemberRecharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 累計充值
        BigDecimal totalRechargeAmount = memberRecharges.stream()
                .map(MemberRecharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 今日提款
        BigDecimal todayWithdrawAmount = memberWithdraws.stream()
                .filter(filter ->
                        filter.getRemitTime().isAfter(LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                                && filter.getRemitTime().isBefore(LocalDateTime.of(LocalDate.now(), LocalTime.MAX)))
                .map(MemberWithdraw::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 累計提款
        BigDecimal totalWithdrawAmount = memberWithdraws.stream()
                .map(MemberWithdraw::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 總存送優惠金額
        BigDecimal rechargeAwardAmount = memberRecharges.stream()
                .map(MemberRecharge::getRechargeAwardAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 存送優惠筆數
        long count = memberRecharges.stream()
                .filter(filter -> Objects.nonNull(filter.getRechargeAwardId())).count();

        // 總領取紅利金額
        BigDecimal awardAmount = memberBonusRecords.stream()
                .map(MemberBonusRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Result.success(
                MemberCapitalSummary.builder()
                        .todayRechargeAmount(todayRechargeAmount)
                        .todayWithdrawAmount(todayWithdrawAmount)
                        .totalRechargeAmount(totalRechargeAmount)
                        .totalWithdrawAmount(totalWithdrawAmount)
                        .rechargeCount(memberRecharges.size())
                        .withdrawCount(memberWithdraws.size())
                        .firstRechargeTime(memberBalance.getFirstRechargeTime())
                        .firstWithdrawTime(memberBalance.getFirstWithdrawTime())
                        .lastRechargeTime(memberBalance.getLastRechargeTime())
                        .lastWithdrawTime(memberBalance.getLastWithdrawTime())
                        .totalRechargeAwardAmount(rechargeAwardAmount)
                        .rechargeAwardCount((int) count)
                        .totalAwardAmount(awardAmount)
                        .awardCount(memberBonusRecords.size())
                        .withdrawControllerState(memberInfoResult.getData().getWithdrawControllerState())
                        .build()
        );
    }


}

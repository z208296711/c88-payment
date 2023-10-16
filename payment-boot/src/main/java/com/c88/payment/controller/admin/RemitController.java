package com.c88.payment.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.c88.affiliate.api.dto.AffiliateMemberWithdrawDTO;
import com.c88.common.core.constant.TopicConstants;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.redis.utils.RedisUtils;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.ExcelExporter;
import com.c88.common.web.util.ExcelReportUtil;
import com.c88.common.web.util.UserUtils;
import com.c88.member.api.MemberFeignClient;
import com.c88.payment.dto.AddAffiliateBalanceDTO;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.enums.AffiliateBalanceChangeTypeEnum;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.FindRemitForm;
import com.c88.payment.pojo.form.RemitForm;
import com.c88.payment.pojo.vo.withdraw.RemitVO;
import com.c88.payment.pojo.vo.withdraw.WithdrawVO;
import com.c88.payment.service.IMemberWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.c88.common.core.constant.TopicConstants.*;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.WITHDRAW;
import static com.c88.common.core.enums.RemitStateEnum.*;
import static com.c88.payment.constants.RedisKey.DAILY_WITHDRAW_AMOUNT;

@Tag(name = "出款處理")
@RestController
@RequestMapping("/api/v1/remit")
@Validated
@AllArgsConstructor
public class RemitController {

    private final IMemberWithdrawService iWithdrawService;
    private final MemberFeignClient memberFeignClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Operation(summary = "出款處理列表")
    @GetMapping("/query")
    public PageResult<RemitVO> query(@ParameterObject FindRemitForm findRemitForm) {
        return PageResult.success(iWithdrawService.queryRemit(findRemitForm));
    }

    @Operation(summary = "匯出出款處理列表")
    @PostMapping("/export")
    public void export(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody FindRemitForm findRemitForm) {
        ExcelExporter.getExcelBySXSSF(iWithdrawService.listRemit(findRemitForm), ExcelReportUtil.ColumnNames.REMIT, request, response,
                "withdraw_" + dateFormat.format(new Date()) + ".xlsx");
    }

    @Operation(summary = "出款申請詳情")
    @GetMapping(value = "/{id}")
    public Result<WithdrawVO> id(@PathVariable Long id) {
        return Result.success(iWithdrawService.getDetail(id));
    }

    @Operation(summary = "領取")
    @PatchMapping("/pick")
    public Result<Boolean> pick(@Valid @RequestBody RemitForm remitForm) {
        if (iWithdrawService.count(new LambdaQueryWrapper<MemberWithdraw>()
                .eq(MemberWithdraw::getRemitUser, UserUtils.getUsername())
                .in(MemberWithdraw::getState, APPLY.getState(), PENDING.getState())) > 0 || // 檢查領取者是否已有其他已領取單
                (iWithdrawService.getById(remitForm.getId()).getRemitUser() != null // 檢查此單是否已有人領取
                )) {
            throw new BizException("error.alreadyPicked");// 已有領取出款單
        }
        return Result.success(iWithdrawService.update(new LambdaUpdateWrapper<MemberWithdraw>()
                .set(MemberWithdraw::getRemitState, PENDING.getState())
                .set(MemberWithdraw::getRemitUser, UserUtils.getUsername())
                .set(MemberWithdraw::getRemitPickTime, LocalDateTime.now())
                .eq(MemberWithdraw::getId, remitForm.getId())));
    }

    @Operation(summary = "出款成功")
    @PatchMapping("/approve")
    public Result<Boolean> approve(@Valid @RequestBody RemitForm remitForm) {
        checkUser(remitForm.getId());

        boolean result = iWithdrawService.update(new LambdaUpdateWrapper<MemberWithdraw>()
                .set(MemberWithdraw::getCompanyBankCardId, remitForm.getCompanyBankCardId())
                .set(MemberWithdraw::getRemitState, SUCCESS.getState())
                .set(MemberWithdraw::getRemitNote, remitForm.getNote())
                .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                .eq(MemberWithdraw::getId, remitForm.getId()));

        // 出款成功寫入代理會員交易紀錄表
        if (result) {
            MemberWithdraw memberWithdraw = iWithdrawService.getById(remitForm.getId());
            kafkaTemplate.send(REMIT,
                    AffiliateMemberWithdrawDTO.builder()
                            .memberId(memberWithdraw.getUid())
                            .username(memberWithdraw.getUsername())
                            .no(memberWithdraw.getWithdrawNo())
                            .amount(memberWithdraw.getAmount())
                            .note(memberWithdraw.getRemitNote())
                            .build()
            );
        }

        return Result.success(result);
    }

    private void checkUser(Long id) {
        MemberWithdraw withdraw = iWithdrawService.getById(id);
        if (!withdraw.getRemitUser().equals(UserUtils.getUsername())) {
            throw new BizException("error.illegalUser");// 非處理人員
        }
    }

    @Operation(summary = "取消出款")
    @PatchMapping("/reject")
    public Result<Boolean> reject(@Valid @RequestBody RemitForm withdrawForm) {
        checkUser(withdrawForm.getId());
        boolean result = iWithdrawService.update(new LambdaUpdateWrapper<MemberWithdraw>()
                .set(MemberWithdraw::getRemitState, CANCEL.getState())
                .set(MemberWithdraw::getRemitNote, withdrawForm.getNote())
                .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                .eq(MemberWithdraw::getId, withdrawForm.getId()));
        if (result) {
            MemberWithdraw withdraw = iWithdrawService.getById(withdrawForm.getId());
            if (withdraw.getType() == 0) {
                // 返回會員申請提款時預先扣除的餘額
                kafkaTemplate.send(BALANCE_CHANGE,
                        AddBalanceDTO.builder()
                                .memberId(withdraw.getUid())
                                .balance(withdraw.getAmount())
                                .balanceChangeTypeLinkEnum(WITHDRAW)
                                .type(WITHDRAW.getType())
                                .betRate(BigDecimal.ZERO)
                                .note(WITHDRAW.getI18n())
                                .build()
                );

                // 返回會員當前等級提款上限金額
                String key = RedisUtils.buildKey(DAILY_WITHDRAW_AMOUNT, withdraw.getGmtCreate().toLocalDate(), withdraw.getUid());
                BigDecimal memberDailyAmount = (BigDecimal) redisTemplate.opsForValue().get(key);
                if (Objects.nonNull(memberDailyAmount)) {
                    redisTemplate.opsForValue().set(key, memberDailyAmount.subtract(withdraw.getAmount()), 3, TimeUnit.DAYS);
                }
            } else {
                kafkaTemplate.send(TopicConstants.AFFILIATE_BALANCE_CHANGE,
                        AddAffiliateBalanceDTO.builder()
                                .affiliateId(withdraw.getUid())
                                .serialNo(withdraw.getWithdrawNo())
                                .type(AffiliateBalanceChangeTypeEnum.WITHDRAW.getValue())
                                .amount(withdraw.getAmount())
                                .note("取消提款返還")
                                .gmtCreate(LocalDateTime.now())
                                .build());
            }
        }
        return Result.success(result);
    }

    @Operation(summary = "撤銷出款")
    @PatchMapping("/revoke")
    public Result<Boolean> revoke(@Valid @RequestBody RemitForm withdrawForm) {
        boolean result = iWithdrawService.update(new LambdaUpdateWrapper<MemberWithdraw>()
                .set(MemberWithdraw::getRemitState, REVOKED.getState())
                .set(MemberWithdraw::getRemitNote, withdrawForm.getNote())
                .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                .set(MemberWithdraw::getRemitUser, UserUtils.getUsername())
                .eq(MemberWithdraw::getId, withdrawForm.getId()));
        if (result) {
            MemberWithdraw withdraw = iWithdrawService.getById(withdrawForm.getId());
            if (withdraw.getType() == 0) {
                // 返回會員申請提款時預先扣除的餘額
                kafkaTemplate.send(BALANCE_CHANGE,
                        AddBalanceDTO.builder()
                                .memberId(withdraw.getUid())
                                .balance(withdraw.getAmount())
                                .balanceChangeTypeLinkEnum(WITHDRAW)
                                .type(WITHDRAW.getType())
                                .betRate(BigDecimal.ZERO)
                                .note(WITHDRAW.getI18n())
                                .build()
                );

                // 返回會員當前等級提款上限金額
                String key = RedisUtils.buildKey(DAILY_WITHDRAW_AMOUNT, withdraw.getGmtCreate().toLocalDate(), withdraw.getUid());
                BigDecimal memberDailyAmount = (BigDecimal) redisTemplate.opsForValue().get(key);
                if (Objects.nonNull(memberDailyAmount)) {
                    redisTemplate.opsForValue().set(key, memberDailyAmount.subtract(withdraw.getAmount()), 3, TimeUnit.DAYS);
                }

                // 出款成功寫入代理會員交易紀錄表
                kafkaTemplate.send(REMIT,
                        AffiliateMemberWithdrawDTO.builder()
                                .memberId(withdraw.getUid())
                                .username(withdraw.getUsername())
                                .no(withdraw.getWithdrawNo())
                                .amount(withdraw.getAmount().negate())
                                .note(withdraw.getRemitNote())
                                .build()
                );
            } else {
                kafkaTemplate.send(TopicConstants.AFFILIATE_BALANCE_CHANGE,
                        AddAffiliateBalanceDTO.builder()
                                .affiliateId(withdraw.getUid())
                                .serialNo(withdraw.getWithdrawNo())
                                .type(AffiliateBalanceChangeTypeEnum.WITHDRAW.getValue())
                                .amount(withdraw.getAmount())
                                .gmtCreate(LocalDateTime.now())
                                .note("取消提款返還")
                                .build());
            }
        }
        return Result.success(result);
    }

    @Operation(summary = "詢問回調")
    @PatchMapping("/checkPayState/{id}")
    public Result<Boolean> checkPayState(@PathVariable Integer id) {
        return Result.success(iWithdrawService.checkPayState(id));
    }

}

package com.c88.payment.controller.admin;

import cn.hutool.core.convert.Convert;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.base.BasePageQuery;
import com.c88.common.core.constant.GlobalConstants;
import com.c88.common.core.constant.TopicConstants;
import com.c88.common.core.enums.WithdrawStateEnum;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.redis.utils.RedisUtils;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.log.OperationEnum;
import com.c88.common.web.util.ExcelExporter;
import com.c88.common.web.util.ExcelReportUtil;
import com.c88.common.web.util.UserUtils;
import com.c88.member.api.MemberFeignClient;
import com.c88.payment.dto.AddAffiliateBalanceDTO;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.enums.AffiliateBalanceChangeTypeEnum;
import com.c88.payment.enums.MemberTypeEnum;
import com.c88.payment.pojo.entity.CommonNote;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.entity.WithdrawRisk;
import com.c88.payment.pojo.form.FindWithdrawForm;
import com.c88.payment.pojo.form.WithdrawForm;
import com.c88.payment.pojo.vo.withdraw.AgentWithdrawReportVO;
import com.c88.payment.pojo.vo.withdraw.WithdrawReportVO;
import com.c88.payment.pojo.vo.withdraw.WithdrawVO;
import com.c88.payment.service.ICommonNoteService;
import com.c88.payment.service.IWithdrawRiskService;
import com.c88.payment.service.IMemberWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.WITHDRAW_FAIL;
import static com.c88.common.core.enums.WithdrawStateEnum.*;
import static com.c88.common.core.result.ResultCode.ACCESS_UNAUTHORIZED;
import static com.c88.payment.constants.RedisKey.DAILY_WITHDRAW_AMOUNT;

@Slf4j
@Tag(name = "風控管理")
@RestController
@RequestMapping("/api/v1/withdraw")
@Validated
@AllArgsConstructor
public class WithdrawController {

    private final IMemberWithdrawService iWithdrawService;
    private final IWithdrawRiskService iWithdrawRiskService;
    private final ICommonNoteService iCommonNoteService;
    private final RedisTemplate redisTemplate;
    private final MemberFeignClient memberFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String FIRST_TRANSFER_PERMISSIONS = "risk:risk-first-review:transfer";
    private static final String SECOND_TRANSFER_PERMISSIONS = "risk:risk-second-review:transfer";

    private static final String FIRST_TRANSFER_PERMISSIONS_AGENT = "agentRisk:risk-first-review:transfer";
    private static final String SECOND_TRANSFER_PERMISSIONS_AGENT = "agentRisk:risk-second-review:transfer";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Operation(summary = "提款申請、提款一審、提款二審列表")
    @GetMapping("/query")
    public PageResult<WithdrawVO> query(@ParameterObject FindWithdrawForm findWithdrawForm) {
        if (findWithdrawForm.getLevel() != null) {
            findWithdrawForm.setCurrentUser(UserUtils.getUsername());// 當查詢一審、二審列表時，當前登入管理帳號處理的提款單會排在第一筆
        }
        return PageResult.success(iWithdrawService.queryWithdraw(findWithdrawForm));
    }

    @Operation(summary = "匯出提款申請列表")
    @PostMapping("/export")
    public void export(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody FindWithdrawForm findWithdrawForm) {
        List<WithdrawReportVO> withdrawReportVOS = iWithdrawService.listWithdraw(findWithdrawForm);
        if (MemberTypeEnum.MEMBER.getValue().equals(findWithdrawForm.getType())) {
            ExcelExporter.getExcelBySXSSF(withdrawReportVOS, ExcelReportUtil.ColumnNames.WITHDRAW, request, response,
                    "withdraw_apply_" + dateFormat.format(new Date()) + ".xlsx");
        } else {
            ExcelExporter.getExcelBySXSSF(
                    withdrawReportVOS.stream().map(w -> JSON.parseObject(JSON.toJSONString(w), AgentWithdrawReportVO.class)).collect(Collectors.toList()),
                    ExcelReportUtil.ColumnNames.WITHDRAW_AGENT, request, response,
                    "withdraw_apply_" + dateFormat.format(new Date()) + ".xlsx"
                    );
        }
    }

    @Operation(summary = "提款申請詳情")
    @GetMapping(value = "/{id}")
    public Result<WithdrawVO> id(@PathVariable Long id) {
        return Result.success(iWithdrawService.getDetail(id));
    }

    @Operation(summary = "領取")
    @PatchMapping("/pick")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#withdrawForm.withdrawNo}",
            desc = "修改了{name} 的類型描述為 {note}",
            menu = "#withdrawForm.type==0 ? 'menu.risk' : 'menu.affiliate'",
            menuPage = "#withdrawForm.type==0 ? #withdrawForm.level==1 ? 'menu.risk-first-review' : 'menu.risk-second-review' : 'menu.risk' ",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "risk_first_verify.operation_log01")
    public Result<Boolean> pick(@Valid @RequestBody WithdrawForm withdrawForm) {
        if (iWithdrawService.count(new LambdaQueryWrapper<MemberWithdraw>()
                .eq(MemberWithdraw::getType,withdrawForm.getType())
                .eq(withdrawForm.getLevel() == 1 ? MemberWithdraw::getFirstUser : MemberWithdraw::getSecondUser, UserUtils.getUsername())
                .notIn(MemberWithdraw::getState, APPROVED.getState(), REJECTED.getState(),
                        withdrawForm.getLevel() == 1 ? SECOND.getState() : APPROVED.getState())) > 0 || // 檢查領取者是否已有其他已領取單
                (withdrawForm.getLevel() == 1 ? iWithdrawService.getById(withdrawForm.getId()).getFirstUser() != null :// 檢查此單是否已有人領取
                        iWithdrawService.getById(withdrawForm.getId()).getSecondUser() != null)) {
            throw new BizException("error.alreadyPicked");// 已有領取提款單
        }
        withdrawForm.setWithdrawNo(iWithdrawService.getById(withdrawForm.getId()).getWithdrawNo());
        return Result.success(iWithdrawService.update(new LambdaUpdateWrapper<MemberWithdraw>()
                .set(withdrawForm.getLevel() == 1 ? MemberWithdraw::getFirstUser : MemberWithdraw::getSecondUser, UserUtils.getUsername())
                .set(withdrawForm.getLevel() == 1 ? MemberWithdraw::getFirstPickTime : MemberWithdraw::getSecondPickTime, LocalDateTime.now())
                .eq(MemberWithdraw::getId, withdrawForm.getId())));
    }

    @Operation(summary = "通過")
    @PatchMapping("/approve")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#withdrawForm.withdrawNo}",
            desc = "修改了 {name} 的類型描述為 {note}",
            menu = "#withdrawForm.type==0 ? 'menu.risk' : 'menu.affiliate'",
            menuPage = "#withdrawForm.type==0 ? #withdrawForm.level==1 ? 'menu.risk-first-review' : 'menu.risk-second-review' : 'menu.risk' ",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "risk_first_verify.operation_log02")
    public Result<Boolean> approve(HttpServletRequest request,
                                   @Valid @RequestBody WithdrawForm withdrawForm) {
        checkUser(withdrawForm.getId(), withdrawForm.getLevel());
        if (withdrawForm.getLevel() == 1 &&
                iWithdrawRiskService.getOne(new LambdaQueryWrapper<WithdrawRisk>()
                        .eq(WithdrawRisk::getWithdrawId, withdrawForm.getId()).last("limit 1")) != null) {
            throw new BizException("error.riskWarning");// 一審的單若風控維度異常則無法直接通過
        }
        withdrawForm.setWithdrawNo(iWithdrawService.getById(withdrawForm.getId()).getWithdrawNo());
        return Result.success(iWithdrawService.approve(withdrawForm, ServletUtil.getClientIP(request)));
    }

    private void checkUser(Long id, int level) {
        MemberWithdraw withdraw = iWithdrawService.getById(id);
        if ((level == 1 && !withdraw.getFirstUser().equals(UserUtils.getUsername())) ||
                (level == 2 && !withdraw.getSecondUser().equals(UserUtils.getUsername()))) {
            throw new BizException("error.illegalUser");// 非處理人員
        }
    }

    @Operation(summary = "拒絕")
    @PatchMapping("/reject")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#withdrawForm.withdrawNo}",
            desc = "修改了 {name} 的類型描述為 {note}",
            menu = "#withdrawForm.type==0 ? 'menu.risk' : 'menu.affiliate'",
            menuPage = "#withdrawForm.type==0 ? #withdrawForm.level==1 ? 'menu.risk-first-review' : 'menu.risk-second-review' : 'menu.risk' ",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "risk_first_verify.operation_log04")
    public Result<Boolean> reject(@Valid @RequestBody WithdrawForm withdrawForm) {
        checkUser(withdrawForm.getId(), withdrawForm.getLevel());
        boolean result = iWithdrawService.update(new LambdaUpdateWrapper<MemberWithdraw>()
                .set(MemberWithdraw::getState, REJECTED.getState())
                .set(withdrawForm.getLevel() == 1 ? MemberWithdraw::getFirstNote : MemberWithdraw::getSecondNote, withdrawForm.getNote())
                .set(withdrawForm.getLevel() == 1 ? MemberWithdraw::getFirstTime : MemberWithdraw::getSecondTime, LocalDateTime.now())
                .eq(MemberWithdraw::getId, withdrawForm.getId()));
        if (result) {
            MemberWithdraw withdraw = iWithdrawService.getById(withdrawForm.getId());

            // 返回會員申請提款時預先扣除的餘額
            if(MemberTypeEnum.MEMBER.getValue().equals(withdrawForm.getType())){
                kafkaTemplate.send(BALANCE_CHANGE,
                        AddBalanceDTO.builder()
                                .memberId(withdraw.getUid())
                                .balance(withdraw.getAmount())
                                .balanceChangeTypeLinkEnum(WITHDRAW_FAIL)
                                .type(WITHDRAW_FAIL.getType())
                                .betRate(BigDecimal.ZERO)
                                .note(WITHDRAW_FAIL.getI18n())
                                .build()
                );
            }else if(MemberTypeEnum.AGENT.getValue().equals(withdrawForm.getType())){
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

            // 返回會員當前等級提款上限金額
            String key = RedisUtils.buildKey(DAILY_WITHDRAW_AMOUNT, withdraw.getGmtCreate().toLocalDate(), withdraw.getUid());
            BigDecimal memberDailyAmount = (BigDecimal) redisTemplate.opsForValue().get(key);
            if (Objects.nonNull(memberDailyAmount)) {
                redisTemplate.opsForValue().set(key, memberDailyAmount.subtract(withdraw.getAmount()), 3, TimeUnit.DAYS);
            }

        }
        withdrawForm.setWithdrawNo(iWithdrawService.getById(withdrawForm.getId()).getWithdrawNo());
        return Result.success(result);
    }

    @Operation(summary = "提交二審")
    @PatchMapping("/second")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#withdrawForm.withdrawNo}",
            desc = "修改了 {name} 的類型描述為 {note}",
            menu = "#withdrawForm.type==0 ? 'menu.risk' : 'menu.affiliate'",
            menuPage = "#withdrawForm.type==0 ? 'menu.risk-first-review':'menu.risk'",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "risk_first_verify.operation_log03")
    public Result<Boolean> second(@Valid @RequestBody WithdrawForm withdrawForm) {
        checkUser(withdrawForm.getId(), 1);
        withdrawForm.setWithdrawNo(iWithdrawService.getById(withdrawForm.getId()).getWithdrawNo());
        return Result.success(iWithdrawService.update(new LambdaUpdateWrapper<MemberWithdraw>()
                .set(MemberWithdraw::getState, SECOND.getState())
                .set(MemberWithdraw::getFirstNote, withdrawForm.getNote())
                .set(MemberWithdraw::getFirstTime, LocalDateTime.now())
                .eq(MemberWithdraw::getId, withdrawForm.getId())));
    }

    @Operation(summary = "轉單")
    @PatchMapping("/transfer")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#withdrawForm.withdrawNo, #withdrawForm.targetUsername}",
            desc = "修改了 {name} 的類型描述為 {note}",
            menu = "#withdrawForm.type==0 ? 'menu.risk' : 'menu.affiliate'",
            menuPage = "#withdrawForm.type==0 ? #withdrawForm.level==1 ? 'menu.risk-first-review' : 'menu.risk-second-review' : 'menu.risk' ",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "risk_first_verify.operation_log05")
    public Result<Boolean> transfer(@Valid @RequestBody WithdrawForm withdrawForm) {
        List<String> roles = UserUtils.getRoles();
        Object object;
        if(MemberTypeEnum.AGENT.getValue().equals(withdrawForm.getType())){
            object = redisTemplate.opsForHash().get(GlobalConstants.BTN_PERM_ROLES_KEY, withdrawForm.getLevel() == 1 ? FIRST_TRANSFER_PERMISSIONS_AGENT : SECOND_TRANSFER_PERMISSIONS_AGENT);
        }else {
            object = redisTemplate.opsForHash().get(GlobalConstants.BTN_PERM_ROLES_KEY, withdrawForm.getLevel() == 1 ? FIRST_TRANSFER_PERMISSIONS : SECOND_TRANSFER_PERMISSIONS);
        }
        List<String> hasPermissionRoles = object != null ? Convert.toList(String.class, object) : null;
        if (object == null ||
                hasPermissionRoles.parallelStream().noneMatch(r -> roles.contains(r))) {// 檢查是否有轉單權限
            throw new BizException(ACCESS_UNAUTHORIZED);
        }
        withdrawForm.setWithdrawNo(iWithdrawService.getById(withdrawForm.getId()).getWithdrawNo());
        return Result.success(iWithdrawService.update(new LambdaUpdateWrapper<MemberWithdraw>()
                .set(withdrawForm.getLevel() == 1 ? MemberWithdraw::getFirstUser : MemberWithdraw::getSecondUser, withdrawForm.getTargetUsername())
                .eq(MemberWithdraw::getId, withdrawForm.getId())));
    }

    @Operation(summary = "常用備註列表")
    @GetMapping(value = {"/note/{level}/query/{memberType}", "/note/{level}/query/{memberType}/{type}"})
    public PageResult<CommonNote> query(@Parameter(description = "審級，1_一審, 2_二審", example = "1") @PathVariable Integer level,
                                        @Parameter(description = "訊息分類，2_轉二審, 3_通過, 4_拒絕", example = "2") @PathVariable(required = false) Byte type,
                                        @ParameterObject BasePageQuery pageQuery, @PathVariable Integer memberType) {
        return PageResult.success(iCommonNoteService.page(
                new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()),
                new LambdaQueryWrapper<CommonNote>()
                        .eq(CommonNote::getLevel, level)
                        .eq(CommonNote::getMemberType, memberType)
                        .eq(WithdrawStateEnum.allNoteTypes().contains(type), CommonNote::getType, type)));
    }

    @Operation(summary = "常用備註清單")
    @GetMapping(value = {"/note/{level}/list/{memberType}", "/note/{level}/list/{memberType}/{type}"})
    public Result<List<CommonNote>> listNotes(@Parameter(description = "審級，1_一審, 2_二審", example = "1") @PathVariable Integer level,
                                              @Parameter(description = "訊息分類，2_轉二審, 3_通過, 4_拒絕", example = "2") @PathVariable(required = false) Byte type,
                                              @PathVariable Integer memberType) {
        return Result.success(iCommonNoteService.list(new LambdaQueryWrapper<CommonNote>()
                .eq(CommonNote::getLevel, level)
                .eq(CommonNote::getMemberType, memberType)
                .eq(WithdrawStateEnum.allNoteTypes().contains(type), CommonNote::getType, type)));
    }

    @Operation(summary = "新增常用備註")
    @PostMapping("/note")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#note.title}",
            desc = "修改了 {name} 的類型描述為 {note}",
            menu = "#note.memberType==0 ? 'menu.risk' : 'menu.affiliate'",
            menuPage = "#note.memberType==0 ? 'menu.risk-first-review' : 'menu.risk'",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "risk_first_verify.operation_log06")
    public Result<Boolean> addNote(@Valid @RequestBody CommonNote note) {
        note.setUpdater(UserUtils.getUsername());
//        note.setUpdater("allen");
        return Result.judge(iCommonNoteService.save(note));
    }

    @Operation(summary = "刪除常用備註")
    @DeleteMapping("/note/{id}")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DELETE,
            content = "new String[]{#note.title}",
            desc = "修改了 {name} 的類型描述為 {note}",
            menu = "#note.memberType==0 ? 'menu.risk' : 'menu.affiliate'",
            menuPage = "#note.memberType==0 ? 'menu.risk-first-review' : 'menu.risk'",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "risk_first_verify.operation_log06")
    public Result<Boolean> deleteNote(@PathVariable Integer id, CommonNote note) {
        CommonNote commonNote = iCommonNoteService.getById(id);
        note.setTitle(iCommonNoteService.lambdaQuery().eq(CommonNote::getId, id).one().getTitle());

        return Result.judge(iCommonNoteService.removeById(id));
    }

    @Operation(summary = "修改常用備註")
    @PutMapping("/note")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DEFAULT,
            content = "new String[]{#note.title}",
            desc = "修改了 {name} 的類型描述為 {note}",
            menu = "#note.memberType==0 ? 'menu.risk' : 'menu.affiliate'",
            menuPage = "#note.memberType==0 ? 'menu.risk-first-review' : 'menu.risk'",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "risk_first_verify.operation_log07")
    public Result<Boolean> updateNote(@Valid @RequestBody CommonNote note) {
        note.setUpdater(UserUtils.getUsername());
        return Result.judge(iCommonNoteService.updateById(note));
    }

}

package com.c88.payment.controller.h5;

import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.base.BaseEntity;
import com.c88.common.core.enums.AccountRecordTimeTypeEnum;
import com.c88.common.core.enums.RemitStateEnum;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.MemberUtils;
import com.c88.payment.enums.MemberAccountWithdrawRecordEnum;
import com.c88.payment.mapstruct.MemberWithdrawConverter;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.ApplyWithdrawFrom;
import com.c88.payment.pojo.form.FindMemberAccountWithdrawRecordForm;
import com.c88.payment.pojo.vo.H5MemberAccountWithdrawRecordVO;
import com.c88.payment.pojo.vo.H5MemberWithdrawVO;
import com.c88.payment.service.MemberWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static com.c88.common.core.enums.RemitStateEnum.CANCEL;
import static com.c88.common.core.enums.RemitStateEnum.PAY_FAILED;
import static com.c88.common.core.enums.RemitStateEnum.PAY_FAILED_REAL_NAME;
import static com.c88.common.core.enums.RemitStateEnum.PAY_PENDING;
import static com.c88.common.core.enums.RemitStateEnum.PAY_SUCCESS;
import static com.c88.common.core.enums.RemitStateEnum.PENDING;
import static com.c88.common.core.enums.RemitStateEnum.REVOKED;
import static com.c88.common.core.enums.RemitStateEnum.SUCCESS;
import static com.c88.common.core.enums.WithdrawStateEnum.APPLY;
import static com.c88.common.core.enums.WithdrawStateEnum.APPROVED;
import static com.c88.common.core.enums.WithdrawStateEnum.REJECTED;
import static com.c88.common.core.enums.WithdrawStateEnum.SECOND;

@Tag(name = "『前台』會員提款")
@RestController
@RequiredArgsConstructor
@RequestMapping("/h5/member/withdraw")
public class MemberWithdrawController {

    private final MemberWithdrawService memberWithdrawService;

    private final MemberWithdrawConverter memberWithdrawConverter;

    @Operation(summary = "檢查提款")
    @GetMapping
    public Result<H5MemberWithdrawVO> findMemberWithdraw() {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(memberWithdrawService.findMemberWithdraw(memberId));
    }

    @Operation(summary = "送出提款申請")
    @PutMapping("/apply")
    public Result<Boolean> applyWithdraw(HttpServletRequest request,
                                         @RequestBody ApplyWithdrawFrom form) {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(memberWithdrawService.applyWithdraw(memberId, form, ServletUtil.getClientIP(request)));
    }

    @Operation(summary = "帳務紀錄-提款", description = "提款")
    @GetMapping("/member/account/withdraw/record")
    public PageResult<H5MemberAccountWithdrawRecordVO> findMemberAccountWithdrawRecord(@Validated @ParameterObject FindMemberAccountWithdrawRecordForm form) {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }

        LambdaQueryChainWrapper<MemberWithdraw> queryWrapper = memberWithdrawService.lambdaQuery();
        switch (MemberAccountWithdrawRecordEnum.getEnum(form.getStatus())) {
            case REVIEW:
                queryWrapper.in(MemberWithdraw::getState, APPLY.getState(), SECOND.getState());
                break;
            case REJECTED:
                queryWrapper.eq(MemberWithdraw::getState, REJECTED.getState());
                break;
            case WITHDRAWING:
                queryWrapper.eq(MemberWithdraw::getState, APPROVED.getState())
                        .in(MemberWithdraw::getRemitState, RemitStateEnum.APPLY.getState(), PENDING.getState(), PAY_PENDING.getState());
                break;
            case SUCCESS:
                queryWrapper.eq(MemberWithdraw::getState, APPROVED.getState())
                        .in(MemberWithdraw::getRemitState, SUCCESS.getState(), PAY_SUCCESS.getState());
                break;
            case FAIL:
                queryWrapper.eq(MemberWithdraw::getState, APPROVED.getState())
                        .in(MemberWithdraw::getRemitState, CANCEL.getState(), PAY_FAILED.getState(), PAY_FAILED_REAL_NAME.getState(), REVOKED.getState());
                break;
        }

        return PageResult.success(
                queryWrapper.eq(MemberWithdraw::getUid, memberId)
                        .between(BaseEntity::getGmtCreate, AccountRecordTimeTypeEnum.getStartDateTime(form.getTimeType()), AccountRecordTimeTypeEnum.getEndDateTime(form.getTimeType()))
                        .orderByDesc(MemberWithdraw::getId)
                        .page(new Page<>(form.getPageNum(), form.getPageSize()))
                        .convert(memberWithdrawConverter::toVO)
        );
    }

}

package com.c88.payment.controller.h5;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.base.BaseEntity;
import com.c88.common.core.enums.AccountRecordTimeTypeEnum;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.redis.annotation.Limit;
import com.c88.common.redis.enums.LimitType;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.MemberUtils;
import com.c88.payment.mapstruct.MemberRechargeConverter;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.entity.RechargeType;
import com.c88.payment.pojo.form.AddOnlineMemberRechargeForm;
import com.c88.payment.pojo.form.FindMemberAccountRechargeRecordForm;
import com.c88.payment.pojo.form.MemberRechargeForm;
import com.c88.payment.pojo.vo.AddOnlineMemberRechargeVO;
import com.c88.payment.pojo.vo.H5MemberAccountRechargeRecordVO;
import com.c88.payment.pojo.vo.H5RechargeVO;
import com.c88.payment.pojo.vo.MemberRechargeFormVO;
import com.c88.payment.service.IMemberRechargeService;
import com.c88.payment.service.IRechargeTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Tag(name = "『前台』會員充值")
@RestController
@RequiredArgsConstructor
@RequestMapping("/h5/memberRecharge")
public class MemberRechargeController {

    private final IMemberRechargeService iRechargeService;

    private final IRechargeTypeService iRechargeTypeService;

    private final MemberRechargeConverter memberRechargeConverter;

    @Operation(summary = "取得-會員可充值渠道")
    @GetMapping
    public Result<H5RechargeVO> findMemberRecharge() {
        H5RechargeVO memberRecharges = iRechargeService.findMemberRecharges(MemberUtils.getMemberId());
        if (memberRecharges.getRechargeTypes().size()==0){
            throw new BizException(ResultCode.NO_COMPANY_CARD_CAN_USED);
        }
        return Result.success(memberRecharges);
    }

    @Operation(summary = "線上存款(第三方充值)")
    @PostMapping(value = "/online")
    @Limit(limitType = LimitType.IP, prefix = "addOnlineMemberRecharge", period = 3, count = 1)
    public Result<AddOnlineMemberRechargeVO> addOnlineMemberRecharge(@Validated @RequestBody AddOnlineMemberRechargeForm form, HttpServletRequest request) {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }

        return Result.success(iRechargeService.addOnlineMemberRecharge(memberId, form, request));
    }

    @Operation(summary = "線下存款(轉自營卡充值)")
    @PostMapping(value = "/inline")
    @Limit(limitType = LimitType.IP, prefix = "inlineRecharge", period = 3, count = 1)
    public Result<MemberRechargeFormVO> inlineRecharge(@Validated @RequestBody MemberRechargeForm form, HttpServletRequest request) {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }

        return Result.success(iRechargeService.inlineRecharge(memberId, form, request));
    }

    @Operation(summary = "帳務紀錄-存款", description = "存款")
    @GetMapping(value = "/member/account/recharge/record")
    public PageResult<H5MemberAccountRechargeRecordVO> findMemberAccountRechargeRecord(@Validated @ParameterObject FindMemberAccountRechargeRecordForm form) {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }

        // 取得商戶名稱
        Map<Integer, String> rechargeTypeMap = iRechargeTypeService.lambdaQuery()
                .select(RechargeType::getId, RechargeType::getName)
                .list()
                .stream()
                .collect(Collectors.toMap(RechargeType::getId, RechargeType::getName));

        return PageResult.success(
                iRechargeService.lambdaQuery()
                        .eq(MemberRecharge::getMemberId, memberId)
                        .eq(form.getStatus() != 0, MemberRecharge::getStatus, form.getStatus())
                        .between(BaseEntity::getGmtCreate, AccountRecordTimeTypeEnum.getStartDateTime(form.getTimeType()), AccountRecordTimeTypeEnum.getEndDateTime(form.getTimeType()))
                        .orderByDesc(MemberRecharge::getId)
                        .page(new Page<>(form.getPageNum(), form.getPageSize()))
                        .convert(memberRecharge -> memberRechargeConverter.toRechargeVO(memberRecharge, rechargeTypeMap))
        );
    }

}

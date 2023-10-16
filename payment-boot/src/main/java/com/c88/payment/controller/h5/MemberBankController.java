package com.c88.payment.controller.h5;

import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.util.MemberUtils;
import com.c88.payment.pojo.form.MemberBankAddForm;
import com.c88.payment.pojo.form.MemberBankModifyForm;
import com.c88.payment.pojo.vo.MemberBankVO;
import com.c88.payment.service.IMemberBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "『前台』會員銀行")
@RestController
@RequiredArgsConstructor
@RequestMapping("/h5/memberBank")
public class MemberBankController {

    private final IMemberBankService iMemberBankService;

    @Operation(summary = "查詢會員銀行")
    @GetMapping
    public Result<List<MemberBankVO>> findMemberBank() {
        Long memberId = MemberUtils.getMemberId();
        if (memberId == null) {
            return Result.failed(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(iMemberBankService.findMemberBank(memberId));
    }

    @Operation(summary = "新增會員銀行")
    @PostMapping
    public Result<Boolean> addMemberBank(@Valid @RequestBody MemberBankAddForm form) {
        Long memberId = MemberUtils.getMemberId();
        if (memberId == null) {
            return Result.failed(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(iMemberBankService.addMemberBank(memberId, form));
    }

    @Operation(summary = "修改會員銀行")
    @PutMapping
    public Result<Boolean> modifyMemberBank(@Valid @RequestBody MemberBankModifyForm form) {
        Long memberId = MemberUtils.getMemberId();
        if (memberId == null) {
            return Result.failed(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(iMemberBankService.modifyMemberBank(memberId, form));
    }

    @Operation(summary = "刪除會員銀行")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMemberBank(@PathVariable("id") Integer id) {
        Long memberId = MemberUtils.getMemberId();
        if (memberId == null) {
            return Result.failed(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(iMemberBankService.deleteMemberBank(id));
    }

    @Operation(summary = "檢查會有有無銀行卡")
    @GetMapping("/checkExist")
    public Result<Boolean> checkMemberBankExist() {
        Long memberId = MemberUtils.getMemberId();
        if (memberId == null) {
            return Result.success(false);
        }
        return Result.success(iMemberBankService.checkMemberBankExist(memberId));
    }

}

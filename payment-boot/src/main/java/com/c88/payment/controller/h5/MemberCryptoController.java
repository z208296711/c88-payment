package com.c88.payment.controller.h5;

import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.util.MemberUtils;
import com.c88.payment.pojo.form.MemberCryptoAddForm;
import com.c88.payment.pojo.form.MemberCryptoModifyForm;
import com.c88.payment.pojo.vo.MemberCryptoVO;
import com.c88.payment.service.IMemberCryptoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "『前台』會員虛擬貨幣")
@RestController
@RequiredArgsConstructor
@RequestMapping("/h5/memberCrypto")
public class MemberCryptoController {

    private final IMemberCryptoService iMemberCryptoService;

    @Operation(summary = "查詢會員虛擬貨幣")
    @GetMapping
    public Result<List<MemberCryptoVO>> findMemberCrypto() {
        Long memberId = MemberUtils.getMemberId();
        if (memberId == null) {
            return Result.failed(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(iMemberCryptoService.findMemberCrypto(memberId));
    }

    @Operation(summary = "新增會員虛擬貨幣")
    @PostMapping
    public Result<Boolean> addMemberCrypto(@Valid @RequestBody MemberCryptoAddForm form) {
        Long memberId = MemberUtils.getMemberId();
        if (memberId == null) {
            return Result.failed(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(iMemberCryptoService.addMemberCrypto(memberId, form));
    }

    @Operation(summary = "修改會員虛擬貨幣")
    @PutMapping
    public Result<Boolean> modifyMemberCrypto(@Valid @RequestBody MemberCryptoModifyForm form) {
        Long memberId = MemberUtils.getMemberId();
        if (memberId == null) {
            return Result.failed(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(iMemberCryptoService.modifyMemberCrypto(form));
    }

    @Operation(summary = "刪除會員虛擬貨幣")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMemberCrypto(@PathVariable("id") Integer id) {
        Long memberId = MemberUtils.getMemberId();
        if (memberId == null) {
            return Result.failed(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(iMemberCryptoService.deleteMemberCrypto(id));
    }

    @Operation(summary = "檢查會員有無虛擬幣地址")
    @GetMapping("/checkExist")
    Result<Boolean> checkMemberCryptoExist() {
        Long memberId = MemberUtils.getMemberId();
        if (memberId == null) {
            return Result.success(false);
        }
        return Result.success(iMemberCryptoService.checkMemberCryptoExist(memberId));
    }


}

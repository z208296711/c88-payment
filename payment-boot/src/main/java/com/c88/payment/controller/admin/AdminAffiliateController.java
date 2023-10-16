package com.c88.payment.controller.admin;

import com.c88.common.core.result.PageResult;
import com.c88.payment.pojo.form.AdminSearchAffiliateMemberTransferForm;
import com.c88.payment.pojo.form.AdminSearchAffiliateTransferForm;
import com.c88.payment.pojo.vo.AdminAffiliateMemberTransferRecordVO;
import com.c88.payment.pojo.vo.AdminAffiliateTransferRecordVO;
import com.c88.payment.service.IAffiliateTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "代理會員標籤")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/affiliate")
public class AdminAffiliateController {

    private final IAffiliateTransferService iAffiliateTransferService;

    @Operation(summary = "查詢-代理佣金轉帳-紀錄")
    @GetMapping("/transfer/record")
    public PageResult<AdminAffiliateTransferRecordVO> transfer(@ParameterObject AdminSearchAffiliateTransferForm form) {
        return PageResult.success(iAffiliateTransferService.findAdminTransferRecordPage(form));
    }

    @Operation(summary = "查詢-會員佣金轉帳-紀錄")
    @GetMapping("/member/transfer/record")
    public PageResult<AdminAffiliateMemberTransferRecordVO> memberTransfer(@ParameterObject AdminSearchAffiliateMemberTransferForm form) {
        return PageResult.success(iAffiliateTransferService.findAdminMemberTransferRecordPage(form));
    }

}

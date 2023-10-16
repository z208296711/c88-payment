package com.c88.payment.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.LogOpResponse;
import com.c88.common.web.log.OperationEnum;
import com.c88.member.vo.OptionVO;
import com.c88.payment.mapstruct.CompanyBankConverter;
import com.c88.payment.pojo.entity.CompanyBankCard;
import com.c88.payment.pojo.form.CompanyBankCardForm;
import com.c88.payment.pojo.vo.CompanyBankCardVO;
import com.c88.payment.service.ICompanyBankCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Tag(name = "『後台』自營卡")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company/bankCard")
public class CompanyBankCardController {

    private final ICompanyBankCardService iCompanyBankCardService;

    private final CompanyBankConverter companyBankConverter;

    @Operation(summary = "自營卡轉卡-銀行卡管理")
    @GetMapping
    public PageResult<CompanyBankCardVO> findCompanyBankCard(@RequestParam(value = "groupId", required = false) Integer groupId,
                                                             @RequestParam(required = false, defaultValue = "1") long pageNum,
                                                             @RequestParam(required = false, defaultValue = "20") long pageSize) {
        return PageResult.success(iCompanyBankCardService.findCompanyCardByGroupId(groupId, new Page<>(pageNum, pageSize)));
    }

    @Operation(summary = "自營卡轉卡-新增")
    @PostMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.INSERT,
            content = "new String[]{#form.code}",
            menu = "menu.finance",
            menuPage = "menu.company-card",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_bankcard.operation_log01")
    public Result<Boolean> addCompanyBankCard(@Validated @RequestBody CompanyBankCardForm form) {
        CompanyBankCard companyBankCard = companyBankConverter.toEntity(form);
        return Result.success(iCompanyBankCardService.save(companyBankCard));
    }

    @Operation(summary = "自營卡轉卡-編輯")
    @PutMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "#form.enableChanged?new String[]{#form.code,#form.enable==0?'停用':'啟用'}:" +
                    "new String[]{#form.code}",
            menu = "menu.finance",
            menuPage = "menu.company-card",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "#form.enableChanged?'be_bankcard.operation_log04':'be_bankcard.operation_log02'")
    public Result<LogOpResponse> modifyCompanyBankCard(@Validated @RequestBody CompanyBankCardForm form) {
        CompanyBankCard before = iCompanyBankCardService.getById(form.getId());
        form.setEnableChanged(!Objects.equals(before.getEnable(), form.getEnable()));
        CompanyBankCard companyBankCard = companyBankConverter.toEntity(form);
        LogOpResponse response = new LogOpResponse();
        if (!form.isEnableChanged()) {
            response.setBefore(before);
            response.setAfter(form);
        }
        iCompanyBankCardService.updateById(companyBankCard);
        return Result.success(response);
    }

    @Operation(summary = "自營卡轉卡-刪除")
    @DeleteMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.DELETE,
            content = "new String[]{#form.code}",
            menu = "menu.finance",
            menuPage = "menu.company-card",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_bankcard.operation_log03")
    public Result<Boolean> delCompanyBankCard(@RequestBody CompanyBankCardForm form) {
        CompanyBankCard before = iCompanyBankCardService.getById(form.getId());
        form.setCode(before.getCode());
        CompanyBankCard companyBankCard = companyBankConverter.toEntity(form);
        return Result.success(iCompanyBankCardService.removeById(companyBankCard));
    }

    @Operation(summary = "自營卡轉卡-取得可使用的全部自營卡")
    @GetMapping("/options")
    public Result<List<OptionVO<Long>>> getAllCompanyBankCard() {
        return Result.success(iCompanyBankCardService.getAllCompanyBankCard());
    }
}

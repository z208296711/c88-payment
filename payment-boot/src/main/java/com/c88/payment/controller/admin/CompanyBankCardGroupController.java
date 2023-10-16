package com.c88.payment.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.OperationEnum;
import com.c88.member.vo.OptionVO;
import com.c88.payment.mapstruct.CompanyBankGroupConverter;
import com.c88.payment.pojo.entity.CompanyBankCardGroup;
import com.c88.payment.pojo.form.CompanyBankCardGroupForm;
import com.c88.payment.pojo.vo.CompanyBankCardGroupVO;
import com.c88.payment.service.ICompanyBankCardGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "『後台』自營卡群組管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company/bankCard/group")
public class CompanyBankCardGroupController {

    private final ICompanyBankCardGroupService iCompanyBankCardGroupService;

    private final CompanyBankGroupConverter companyBankGroupConverter;

    @Operation(summary = "自營卡群組管理-查找")
    @GetMapping
    public PageResult<CompanyBankCardGroupVO> findCompanyBankCardGroup(@RequestParam(value = "name", required = false) String name,
                                                                       @RequestParam(required = false, defaultValue = "1") long pageNum,
                                                                       @RequestParam(required = false, defaultValue = "20") long pageSize) {
        return PageResult.success(iCompanyBankCardGroupService.findCompanyCardGroup(name, new Page<>(pageNum, pageSize)));
    }

    @Operation(summary = "自營卡群組管理option-查找")
    @GetMapping("/option")
    public Result<List<OptionVO<Long>>> findCompanyBankCardGroupOption() {
        return Result.success(iCompanyBankCardGroupService
                .lambdaQuery()
                .select(CompanyBankCardGroup::getId, CompanyBankCardGroup::getName)
                .orderByAsc(CompanyBankCardGroup::getId)
                .list()
                .stream()
                .map(x -> new OptionVO<>(x.getId(), x.getName()))
                .collect(Collectors.toList()));
    }

    @Operation(summary = "自營卡群組管理-新增")
    @PostMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.INSERT,
            content = "new String[]{#form.name}",
            menu = "menu.finance",
            menuPage = "menu.company-card",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_bankcard_group.operation_log01")
    public Result<Boolean> addCompanyBankCardGroup(@Validated @RequestBody CompanyBankCardGroupForm form) {
        CompanyBankCardGroup companyBankCardGroup = companyBankGroupConverter.toEntity(form);
        return Result.success(iCompanyBankCardGroupService.save(companyBankCardGroup));
    }

    @Operation(summary = "自營卡群組管理-編輯")
    @PutMapping
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.beforeName,#form.name,#form.note}",
            menu = "menu.finance",
            menuPage = "menu.company-card",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "be_bankcard_group.operation_log02")
    public Result<Boolean> modifyCompanyBankCardGroup(@Validated @RequestBody CompanyBankCardGroupForm form) {
        CompanyBankCardGroup before = iCompanyBankCardGroupService.getById(form.getId());
        form.setBeforeName(before.getName());
        CompanyBankCardGroup companyBankCardGroup = companyBankGroupConverter.toEntity(form);
        return Result.success(iCompanyBankCardGroupService.updateById(companyBankCardGroup));
    }

}

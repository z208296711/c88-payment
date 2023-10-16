package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.common.core.constant.GlobalConstants;
import com.c88.common.web.enums.SortTypeEnum;
import com.c88.member.vo.OptionVO;
import com.c88.payment.constants.RedisKey;
import com.c88.payment.dto.BankDTO;
import com.c88.payment.enums.BankStateEnum;
import com.c88.common.core.enums.EnableEnum;
import com.c88.payment.enums.SearchBankTypeEnum;
import com.c88.payment.mapper.BankMapper;
import com.c88.payment.mapstruct.AdminBankConverter;
import com.c88.payment.mapstruct.BankConverter;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.entity.MerchantRechargeChannel;
import com.c88.payment.pojo.form.*;
import com.c88.payment.pojo.vo.AdminBankVO;
import com.c88.payment.pojo.vo.BankVO;
import com.c88.payment.service.IBankService;
import com.c88.payment.service.IMerchantRechargeChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankServiceImpl extends ServiceImpl<BankMapper, Bank> implements IBankService {

    private final BankConverter bankConverter;

    private final AdminBankConverter adminBankConverter;

    private final IMerchantRechargeChannelService iMerchantRechargeChannelService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<BankVO> findBank() {
        return this.lambdaQuery()
                .list()
                .stream()
                .sorted(Comparator.comparing(Bank::getSort))
                .map(bankConverter::toVo)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "payment", key = "'banks'")
    public List<BankDTO> findBankDTO() {
        return this.lambdaQuery()
                .list()
                .stream()
                .map(bankConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public IPage<AdminBankVO> findAdminBankPage(AdminSearchBankForm form) {
        SearchBankTypeEnum searchBankType = SearchBankTypeEnum.getEnum(form.getSearchBankType());

        LambdaQueryChainWrapper<Bank> bankLambdaQueryChainWrapper = this.lambdaQuery()
                .eq(StringUtils.isNotBlank(form.getCode()), Bank::getCode, form.getCode())
                .eq(Objects.nonNull(form.getState()), Bank::getState, form.getState())
                .orderByAsc(Bank::getSort);

        if (Objects.equals(searchBankType, SearchBankTypeEnum.MERCHANT_RECHARGE_USE_BANK)) {
            Set<Long> bankIds = iMerchantRechargeChannelService.lambdaQuery()
                    .select(MerchantRechargeChannel::getBankId).list().stream().map(MerchantRechargeChannel::getBankId).collect(Collectors.toUnmodifiableSet());
            bankLambdaQueryChainWrapper.eq(CollectionUtils.isNotEmpty(bankIds), Bank::getId, bankIds);
        }

        return bankLambdaQueryChainWrapper.page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(adminBankConverter::entityToAdminBankVO);
    }

    @Override
    @Transactional
    public Boolean addBank(AddBankForm form) {
        Bank bank = adminBankConverter.fromToEntity(form);
        this.save(bank);
        return this.baseMapper.modifyBankSortTop(bank.getId());
    }

    @Transactional
    public Boolean modifyBankMaintainTime(MaintainBankForm form) {
        Bank bank = Bank.builder()
                .id(form.getId())
                .assignEnable(form.getAssignEnable())
                .assignStartTime(form.getAssignStartTime())
                .assignEndTime(form.getAssignEndTime())
                .dailyEnable(form.getDailyEnable())
                .dailyStartTime(form.getDailyStartTime())
                .dailyEndTime(form.getDailyEndTime())
                .build();

        bank.setState(getBankState(bank).getValue());

        boolean isSuccess = this.updateById(bank);

        if (isSuccess) {
            bankMaintainTimeProcess(bankConverter.toMaintainBankForm(bank));
        }

        return isSuccess;
    }

    public void bankMaintainTimeProcess(MaintainBankForm form) {
        LocalDateTime nowTime = LocalDateTime.now();

        // 判斷指定維護時間是否開啟,狀態為啟用才需判斷
        if (form.getAssignEnable().equals(GlobalConstants.STATUS_YES)) {
            Duration assignStartTime = Duration.between(nowTime, form.getAssignStartTime());
            Duration assignEndTime = Duration.between(nowTime, form.getAssignEndTime());
            redisTemplate.opsForValue().set(RedisKey.BANK_MAINTAIN + ":" + form.getId() + ":AssignStart", 1, assignStartTime.getSeconds() < 1 ? 1 : assignStartTime.getSeconds() + 1, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(RedisKey.BANK_MAINTAIN + ":" + form.getId() + ":AssignEnd", 1, assignEndTime.getSeconds() < 1 ? 1 : assignEndTime.getSeconds() + 1, TimeUnit.SECONDS);
        }

        // 判斷每日維護時間是否開啟,狀態為啟用才需判斷
        if (form.getDailyEnable().equals(GlobalConstants.STATUS_YES)) {
            LocalDateTime dailyStartTime = LocalDateTime.of(LocalDate.now(), form.getDailyStartTime());
            LocalDateTime dailyEndTime = LocalDateTime.of(LocalDate.now(), form.getDailyEndTime());
            if (nowTime.compareTo(dailyEndTime) > 0) {
                dailyStartTime = dailyStartTime.plusDays(1);
                dailyEndTime = dailyEndTime.plusDays(1);
            }
            Duration startToNowTime = Duration.between(nowTime, dailyStartTime);
            Duration nowToEndTime = Duration.between(nowTime, dailyEndTime);
            redisTemplate.opsForValue().set(RedisKey.BANK_MAINTAIN + ":" + form.getId() + ":DailyStart", 1, startToNowTime.getSeconds() < 1 ? 1 : startToNowTime.getSeconds() + 1, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(RedisKey.BANK_MAINTAIN + ":" + form.getId() + ":DailyEnd", 1, nowToEndTime.getSeconds() < 1 ? 1 : nowToEndTime.getSeconds() + 1, TimeUnit.SECONDS);
        }
    }

    public BankStateEnum getBankState(Bank bank) {
        LocalDateTime now = LocalDateTime.now();
        BankStateEnum state = BankStateEnum.ENABLE;

        // 指定維護及每日維護開關皆關閉時直接回傳啟用狀態
        if (Objects.equals(bank.getAssignEnable(), EnableEnum.STOP.getCode()) && Objects.equals(bank.getDailyEnable(), EnableEnum.STOP.getCode())) {
            return state;
        }

        // 判斷指定日期時間 啟動時才需判斷
        if (Objects.equals(bank.getAssignEnable(), EnableEnum.START.getCode()) && now.compareTo(bank.getAssignEndTime()) < 0) {
            if (now.compareTo(bank.getAssignStartTime()) > 0 && now.compareTo(bank.getAssignEndTime()) < 0) {
                return BankStateEnum.MAINTAINING;
            } else {
                state = BankStateEnum.SCHEDULED;
            }
        }

        // 判斷每日排程時間 啟動時才需判斷
        if (Objects.equals(bank.getDailyEnable(), EnableEnum.START.getCode())) {
            LocalDateTime startTime = LocalDateTime.of(LocalDate.now(), bank.getDailyStartTime());
            LocalDateTime endTime = LocalDateTime.of(LocalDate.now(), bank.getDailyEndTime());
            if (endTime.isBefore(startTime)) {
                startTime = startTime.minusDays(1);
            }
            if (now.compareTo(startTime) > 0 && now.compareTo(endTime) < 0) {
                return BankStateEnum.MAINTAINING;
            } else {
                state = BankStateEnum.SCHEDULED;
            }
        }

        return state;
    }

    @Override
    public Boolean modifyBankSort(ModifyBankSortForm form) {
        return this.updateBatchById(
                form.getBankSortReq()
                        .stream()
                        .map(bankConverter::toEntity)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    @Override
    public List<OptionVO<Long>> findBankOption() {
        return this.lambdaQuery()
                .select(Bank::getId, Bank::getCode, Bank::getSort)
                .list()
                .stream()
                .sorted(Comparator.comparing(Bank::getSort))
                .map(x ->
                        OptionVO.<Long>builder()
                                .value(x.getId())
                                .label(x.getCode())
                                .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<OptionVO<Long>> findBankOptionByMerchantIdOption(Long merchantId) {
        Set<Long> bankIds = iMerchantRechargeChannelService.lambdaQuery()
                .select(MerchantRechargeChannel::getBankId)
                .eq(MerchantRechargeChannel::getMerchantId, merchantId)
                .list()
                .stream()
                .map(MerchantRechargeChannel::getBankId)
                .collect(Collectors.toSet());

        return this.lambdaQuery()
                .in(Bank::getId, bankIds)
                .list()
                .stream()
                .map(x ->
                        OptionVO.<Long>builder()
                                .value(x.getId())
                                .label(x.getCode())
                                .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "payment", allEntries = true)
    })
    public Boolean modifyBank(ModifyBankForm form) {
        Bank byId = this.getById(form.getId());
        Bank bank = adminBankConverter.fromToEntity(form);
        bank.setAssignStartTime(byId.getAssignStartTime());
        bank.setAssignEndTime(byId.getAssignEndTime());
        return this.updateById(bank);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "payment", allEntries = true)
    })
    public Boolean modifyBank(Bank bank) {
        return this.save(bank);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "payment", allEntries = true)
    })
    public Boolean modifyBank(List<Bank> bank) {
        return this.updateBatchById(bank);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "payment", allEntries = true)
    })
    public Boolean modifyBankSortTopBottom(ModifyBankSortTopBottomForm form) {
        SortTypeEnum sortType = SortTypeEnum.getEnum(form.getSortType());
        switch (sortType) {
            case TOP:
                return this.baseMapper.modifyBankSortTop(form.getId());
            case BOTTOM:
                return this.baseMapper.modifyBankSortBottom(form.getId());
            default:
                return Boolean.FALSE;
        }
    }
}





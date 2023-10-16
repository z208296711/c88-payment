package com.c88.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.redis.utils.RedisUtils;
import com.c88.common.web.exception.BizException;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.enums.MemberTypeEnum;
import com.c88.payment.mapper.WithdrawMapper;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.entity.MemberBalance;
import com.c88.payment.pojo.entity.MemberBank;
import com.c88.payment.pojo.entity.MemberCrypto;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.entity.MemberWithdrawBet;
import com.c88.payment.pojo.form.ApplyWithdrawFrom;
import com.c88.payment.pojo.vo.H5MemberBankVO;
import com.c88.payment.pojo.vo.H5MemberWithdrawVO;
import com.c88.payment.pojo.vo.H5memberCryptoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.c88.common.core.constant.TopicConstants.WITHDRAW_APPLY;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.WITHDRAW;
import static com.c88.common.core.enums.WithdrawStateEnum.APPROVED;
import static com.c88.common.core.enums.WithdrawStateEnum.REJECTED;
import static com.c88.common.web.util.UUIDUtils.genWithdrawOrderId;
import static com.c88.payment.constants.RedisKey.DAILY_WITHDRAW_AMOUNT;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberWithdrawService extends ServiceImpl<WithdrawMapper, MemberWithdraw> {

    private final IMemberWithdrawBetService iMemberWithdrawBetService;
    private final MemberFeignClient memberFeignClient;

    private final IMemberBankService iMemberBankService;

    private final IMemberCryptoService iMemberCryptoService;

    private final IBankService iBankService;

    private final PasswordEncoder passwordEncoder;

    private final IMemberBalanceService iMemberBalanceService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

    public H5MemberWithdrawVO findMemberWithdraw(Long memberId) {
        Result<MemberInfoDTO> memberResult = memberFeignClient.getMemberInfo(memberId);
        if (!Result.isSuccess(memberResult)) {
            throw new BizException(ResultCode.RESOURCE_NOT_FOUND);
        }

        MemberBalance memberBalance = iMemberBalanceService.findByMemberId(memberId);

        List<Bank> banks = iBankService.list();

        List<H5MemberBankVO> memberBankVOS = iMemberBankService.lambdaQuery()
                .eq(MemberBank::getMemberId, memberId)
                .eq(MemberBank::getEnable, EnableEnum.START.getCode())
                .list()
                .stream().map(memberBank -> {
                            Bank bank = banks.stream()
                                    .filter(filter -> Objects.equals(filter.getId().intValue(), memberBank.getBankId()))
                                    .findFirst()
                                    .orElse(Bank.builder().build());

                            return H5MemberBankVO.builder()
                                    .memberBankId(memberBank.getId())
                                    .memberBankCardNo(memberBank.getCardNo())
                                    .startTime(bank.getDailyEnable() != 0 ? timeFormatter.format(bank.getDailyStartTime()) :
                                            (bank.getAssignEnable() != 0 ? dateTimeFormatter.format(bank.getAssignStartTime()) : null))
                                    .endTime(bank.getDailyEnable() != 0 ? timeFormatter.format(bank.getDailyEndTime()) :
                                            (bank.getAssignEnable() != 0 ? dateTimeFormatter.format(bank.getAssignEndTime()) : null))
                                    .state(bank.getState()).build();
                        }
                )
                .collect(Collectors.toList());

        List<H5memberCryptoVO> memberCryptoVOS = iMemberCryptoService.lambdaQuery()
                .eq(MemberCrypto::getMemberId, memberId)
                .eq(MemberCrypto::getEnable, EnableEnum.START.getCode())
                .list()
                .stream().map(memberBank -> H5memberCryptoVO.builder().build())
                .collect(Collectors.toList());
        MemberWithdrawBet memberWithdrawBet = iMemberWithdrawBetService.findMemberWithdrawBet(memberBalance.getMemberId(), memberBalance.getUsername());
        BigDecimal withdrawLimit = memberWithdrawBet.getAccBet().subtract(memberWithdrawBet.getValidBet());
        return H5MemberWithdrawVO.builder()
                .balance(memberBalance.getBalance())
                .withdrawLimit(withdrawLimit.compareTo(BigDecimal.ZERO) >= 0 ? withdrawLimit : BigDecimal.ZERO)
                .memberBank(memberBankVOS)
                .memberCrypto(memberCryptoVOS)
                .build();
    }

    public boolean applyWithdraw(Long uid, ApplyWithdrawFrom form, String ip) {
        MemberInfoDTO member = memberFeignClient.getMemberInfo(uid).getData();
        MemberBalance memberBalance = iMemberBalanceService.findByMemberId(uid);
        if (memberBalance.getBalance().compareTo(form.getAmount()) < 0) {
            throw new BizException("home.alert001");// 會員餘額不足
        }
        if (isEmpty(member.getWithdrawPassword())) {
            throw new BizException("withdrawal.title009");// 設置提款密碼後才可提款
        }

        // 檢查提款狀態 0關閉 1開啟
        if (member.getWithdrawControllerState() == 0) {
            throw new BizException("withdrawal.title011");
        }

        if (!passwordEncoder.matches(form.getWithdrawPassword(), member.getWithdrawPassword())) {
            throw new BizException(ResultCode.WITHDRAW_PASSWORD_ERROR);
        }
        if (getOne(new LambdaQueryWrapper<MemberWithdraw>()
                .eq(MemberWithdraw::getUid, uid)
                .notIn(MemberWithdraw::getState, APPROVED.getState(), REJECTED.getState())
                .last("limit 1")) != null) {// 會員同一時間只能申請一個提款
            throw new BizException("error.alreadyWithdrawal");
        }

        MemberWithdrawBet memberWithdrawBet = iMemberWithdrawBetService.findMemberWithdrawBet(memberBalance.getMemberId(), memberBalance.getUsername());
        BigDecimal withdrawLimit = memberWithdrawBet.getAccBet().subtract(memberWithdrawBet.getValidBet());
        if (withdrawLimit.compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException("withdrawal.toast01");
        }

        MemberInfoDTO memberInfo = memberFeignClient.getMemberInfo(uid).getData();

        // 檢查當前vip提款上限
        LocalDate now = LocalDate.now(ZoneId.of("+7"));
        String key = RedisUtils.buildKey(DAILY_WITHDRAW_AMOUNT, now, uid);

        BigDecimal memberDailyAmount = (BigDecimal) redisTemplate.opsForValue().get(key);
        // 會員當日充值的金額加上此次要充值的金額
        BigDecimal afterAmount = form.getAmount();

        // 會員當日已充值的金額
        if (memberDailyAmount != null) {
            afterAmount = memberDailyAmount.add(afterAmount);
        }

        if (memberInfo.getDailyWithdrawAmount().compareTo(BigDecimal.ZERO) != 0 && afterAmount.compareTo(memberInfo.getDailyWithdrawAmount()) > 0) {
            throw new BizException(ResultCode.DAILY_WITHDRAW_AMOUNT_INVALID);
        }
        redisTemplate.opsForValue().set(key, afterAmount, 3, TimeUnit.DAYS);

        MemberWithdraw withdraw = new MemberWithdraw();
        withdraw.setType(MemberTypeEnum.MEMBER.getValue());
        withdraw.setAmount(form.getAmount());
        withdraw.setWithdrawNo(genWithdrawOrderId());
        withdraw.setUid(uid);
        withdraw.setVipId(memberInfo.getCurrentVipId());
        withdraw.setTagIds(memberInfo.getTagIdList().toArray(new Integer[]{}));
        withdraw.setApplyTime(LocalDateTime.now());
        withdraw.setApplyIp(ip);
        withdraw.setUsername(memberBalance.getUsername());
        withdraw.setRealName(member.getRealName());
        withdraw.setCurrentBalance(memberBalance.getBalance().subtract(form.getAmount()));
        withdraw.setBankId(form.getMemberBankId());
        withdraw.setBankCardNo(form.getMemberBankCardNo());
        withdraw.setCryptoName(form.getCryptoName());
        withdraw.setCryptoAddress(form.getCryptoAddress());
        boolean result = save(withdraw);
        if (result) {
            // 預先扣除用戶餘額
            iMemberBalanceService.addBalance(
                    AddBalanceDTO.builder()
                            .memberId(memberBalance.getMemberId())
                            .balance(withdraw.getAmount().negate())
                            .type(WITHDRAW.getType())
                            .betRate(BigDecimal.ZERO)
                            .note(WITHDRAW.getI18n())
                            .build()
            );

            kafkaTemplate.send(WITHDRAW_APPLY, withdraw);
        }

        return result;
    }
}

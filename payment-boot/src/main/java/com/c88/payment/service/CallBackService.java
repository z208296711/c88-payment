package com.c88.payment.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.c88.common.core.enums.RechargeAwardTypeEnum;
import com.c88.common.core.enums.RemitStateEnum;
import com.c88.common.core.result.ResultCode;
import com.c88.common.core.util.AESUtil;
import com.c88.common.redis.utils.RedisUtils;
import com.c88.common.web.exception.BizException;
import com.c88.member.enums.RechargeAwardRecordModelActionEnum;
import com.c88.member.enums.RechargeAwardRecordStateEnum;
import com.c88.member.event.RechargeAwardRecordModel;
import com.c88.member.vo.MemberRechargeAwardTemplateClientVO;
import com.c88.payment.constants.Third.FBPayConstant;
import com.c88.payment.dto.AddBalanceDTO;
import com.c88.payment.enums.PayStatusEnum;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.*;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.form.CallBackFBForm;
import com.c88.payment.pojo.form.CallBackFBehalfPayForm;
import com.c88.payment.pojo.form.CallBackFrom;
import com.c88.payment.pojo.form.CallBackLidaForm;
import com.c88.payment.pojo.form.CallBackPrinceForm;
import com.c88.payment.pojo.form.CallBackPrinceResult;
import com.c88.payment.pojo.form.CallBackTBForm;
import com.c88.payment.pojo.form.CallBackWuBaForm;
import com.c88.payment.pojo.form.CallBackWuBaPayForm;
import com.c88.payment.vo.RechargeAwardDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;
import static com.c88.common.core.constant.TopicConstants.RECHARGE_AWARD_RECORD;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.OUT_RECHARGE;
import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.WITHDRAW_FAIL;
import static com.c88.payment.constants.RedisKey.DAILY_WITHDRAW_AMOUNT;
import static com.c88.payment.constants.thirdparty.WuBaPayConstant.SUCCESS_CALL_BACK;
import static com.c88.payment.constants.Third.FBPayConstant.RECHARGE_STATE_MAP;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallBackService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final IMemberRechargeService iRechargeService;

    private final IMemberWithdrawService iWithdrawService;

    private final IMemberRechargeService iMemberRechargeService;

    private final IMerchantService iMerchantService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 成功
     */
    private static final Integer ORDER_SUCCESS = 10000;

    /**
     * 交易失败
     */
    private static final Integer ORDER_FAIL = 30916;

    /**
     * 成功回傳
     */
    private static final String SUCCESS_RESULT = "success";

    @Transactional
    public String callBackPrince(CallBackPrinceForm form) {
        log.info("=====prince form : {}", form);

        // 回傳狀態成功或失敗才繼續
        if (!List.of(ORDER_SUCCESS, ORDER_FAIL).contains(form.getStatus())) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }

        // 寫入redis避免短時間重複執行
        if (Boolean.TRUE.equals(redisTemplate.hasKey(form.getSign()))) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        redisTemplate.opsForValue().set(form.getSign(), Boolean.TRUE, 10, TimeUnit.SECONDS);

        // 檢查太子支付的資料
        if (Boolean.FALSE.equals(checkPrinceData(form))) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }

        CallBackPrinceResult result = JSON.parseObject(form.getResult(), CallBackPrinceResult.class);

        CallBackFrom callBackFrom = CallBackFrom.builder()
                .transactionId(String.valueOf(result.getTransactionId()))
                .orderId(result.getOrderId())
                .amount(result.getAmount())
                .realAmount(result.getRealAmount())
                .note(result.getCustom())
                .build();

        // 訂單支付成功
        if (Objects.equals(form.getStatus(), ORDER_SUCCESS)) {
            return orderSuccessProcess(callBackFrom);
        }

        // 訂單支付失敗
        return orderFailProcess(callBackFrom);
    }

    public String callBackPrincePay(CallBackPrinceForm form) {
        log.info("=====prince form pay : {}", form);

        // 寫入redis避免短時間重複執行
        if (Boolean.TRUE.equals(redisTemplate.hasKey(form.getSign()))) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        redisTemplate.opsForValue().set(form.getSign(), Boolean.TRUE, 10, TimeUnit.SECONDS);

        // 檢查太子支付的資料
        if (Boolean.FALSE.equals(checkPrinceData(form))) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }

        CallBackPrinceResult callBackPrinceResult = JSON.parseObject(form.getResult(), CallBackPrinceResult.class);
        log.info("Prince flow : {}", callBackPrinceResult);

        MemberWithdraw memberWithdraw = iWithdrawService.lambdaQuery()
                .eq(MemberWithdraw::getWithdrawNo, callBackPrinceResult.getOrderId())
                .eq(MemberWithdraw::getRemitState, RemitStateEnum.PAY_PENDING.getState())
                .oneOpt()
                .orElseThrow(() -> new BizException("Prince 無此代付訂單"));

        // 代付訂單成功
        if (Objects.equals(form.getStatus(), ORDER_SUCCESS)) {
            log.info("Prince代付成功 success before {}", callBackPrinceResult);

            iWithdrawService.lambdaUpdate()
                    .eq(MemberWithdraw::getWithdrawNo, memberWithdraw.getWithdrawNo())
                    .set(MemberWithdraw::getRemitState, RemitStateEnum.PAY_SUCCESS.getState())
                    .set(MemberWithdraw::getRemitNote, "自動代付成功")
                    .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                    .update();

            log.info("Prince代付成功 success after {}", callBackPrinceResult);

            return SUCCESS_RESULT;
        }

        // 代付訂單失敗
        log.info("Prince代付失敗 fail after {}", callBackPrinceResult);

        // 返回會員當前等級提款上限金額
        String key = RedisUtils.buildKey(DAILY_WITHDRAW_AMOUNT, memberWithdraw.getGmtCreate().toLocalDate(), memberWithdraw.getUid());
        BigDecimal memberDailyAmount = (BigDecimal) redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(memberDailyAmount)) {
            redisTemplate.opsForValue().set(key, memberDailyAmount.subtract(memberWithdraw.getAmount()), 3, TimeUnit.DAYS);
        }

        boolean result = iWithdrawService.lambdaUpdate()
                .eq(MemberWithdraw::getWithdrawNo, callBackPrinceResult.getOrderId())
                .set(MemberWithdraw::getRemitState, RemitStateEnum.PAY_FAILED.getState())
                .set(MemberWithdraw::getRemitNote, "自動代付失敗")
                .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                .update();

        if (result) {
            // 返回會員申請提款時預先扣除的餘額
            kafkaTemplate.send(BALANCE_CHANGE,
                    AddBalanceDTO.builder()
                            .memberId(memberWithdraw.getUid())
                            .balance(memberWithdraw.getAmount())
                            .balanceChangeTypeLinkEnum(WITHDRAW_FAIL)
                            .type(WITHDRAW_FAIL.getType())
                            .betRate(BigDecimal.ZERO)
                            .note(WITHDRAW_FAIL.getI18n())
                            .build()
            );
        }

        log.info("Prince代付失敗 fail after {}", callBackPrinceResult);

        return SUCCESS_RESULT;
    }

    public String orderFailProcess(CallBackFrom form) {
        iRechargeService.lambdaUpdate()
                .eq(MemberRecharge::getTradeNo, form.getOrderId())
                .set(MemberRecharge::getStatus, PayStatusEnum.FAIL.getValue())
                .update();

        return SUCCESS_RESULT;
    }

    public String orderSuccessProcess(CallBackFrom form) {
        // 查詢訂單
        MemberRecharge memberRecharge = iRechargeService.lambdaQuery()
                .eq(MemberRecharge::getTradeNo, form.getOrderId())
                .eq(MemberRecharge::getStatus, PayStatusEnum.IN_PROGRESS.getValue())
                .oneOpt()
                .orElseThrow(() -> new BizException("無此訂單"));

        BigDecimal amount = memberRecharge.getAmount()
                .subtract(memberRecharge.getFee())
                .add(memberRecharge.getRechargeAwardAmount());

        // 取得存送優惠資訊
        Optional<MemberRechargeAwardTemplateClientVO> memberRechargeAwardTemplateOpt = Optional.ofNullable(iMemberRechargeService.getMemberRechargeAwardTemplateClientVO(memberRecharge.getRechargeAwardId(), memberRecharge.getAmount()));

        // 訂單成立用戶充值及增加提領限額
        kafkaTemplate.send(BALANCE_CHANGE,
                AddBalanceDTO.builder()
                        .memberId(memberRecharge.getMemberId())
                        .balance(amount)
                        .balanceChangeTypeLinkEnum(OUT_RECHARGE)
                        .type(OUT_RECHARGE.getType())
                        .betRate(BigDecimal.ONE)
                        .note(OUT_RECHARGE.getI18n())
                        .rechargeAwardDTO(
                                memberRechargeAwardTemplateOpt.map(template ->
                                                RechargeAwardDTO.builder()
                                                        .templateId(template.getId())
                                                        .name(template.getName())
                                                        .type(template.getType())
                                                        .mode(template.getMode())
                                                        .betRate(template.getBetRate())
                                                        .rate(template.getRate())
                                                        .fixAmount(template.getAmount())
                                                        .minJoinAmount(template.getMinJoinAmount())
                                                        .maxAwardAmount(template.getMaxAwardAmount())
                                                        .build()
                                        )
                                        .orElse(null)
                        )
                        .build()
        );

        // 修改訂單狀態
        iRechargeService.lambdaUpdate()
                .eq(MemberRecharge::getId, memberRecharge.getId())
                .set(MemberRecharge::getStatus, PayStatusEnum.SUCCESS.getValue())
                .set(MemberRecharge::getOutTradeNo, form.getTransactionId())
                .set(MemberRecharge::getRealAmount, amount.add(memberRecharge.getRechargeAwardAmount()))
                .set(MemberRecharge::getSuccessTime, LocalDateTime.now())
                .set(MemberRecharge::getOrderAmount, form.getAmount())
                .set(MemberRecharge::getOrderRealAmount, form.getRealAmount())
                .update();

        if (memberRechargeAwardTemplateOpt.isPresent()) {
            MemberRechargeAwardTemplateClientVO memberRechargeAwardTemplate = memberRechargeAwardTemplateOpt.get();
            kafkaTemplate.send(RECHARGE_AWARD_RECORD,
                    RechargeAwardRecordModel.builder()
                            .action(Objects.equals(memberRechargeAwardTemplate.getType(), RechargeAwardTypeEnum.PLATFORM.getCode()) ?
                                    RechargeAwardRecordModelActionEnum.ADD.getCode() :
                                    RechargeAwardRecordModelActionEnum.MODIFY.getCode()
                            )
                            .memberId(memberRecharge.getMemberId())
                            .username(memberRecharge.getUsername())
                            .templateId(memberRechargeAwardTemplate.getId())
                            .name(memberRechargeAwardTemplate.getName())
                            .type(memberRechargeAwardTemplate.getType())
                            .mode(memberRechargeAwardTemplate.getMode())
                            .rate(memberRechargeAwardTemplate.getRate())
                            .betRate(memberRechargeAwardTemplate.getBetRate())
                            .amount(memberRechargeAwardTemplate.getAmount())
                            .rechargeAmount(memberRecharge.getAmount())
                            .useTime(LocalDateTime.now())
                            .state(RechargeAwardRecordStateEnum.USED.getCode())
                            .build()
            );
        }

        return SUCCESS_RESULT;
    }

    /**
     * 檢查太子支付簽名
     *
     * @param form
     * @return
     */
    private Boolean checkPrinceData(CallBackPrinceForm form) {
        return Boolean.TRUE;
    }

    public String callBackLida(CallBackLidaForm form) {
        log.info("callBackLida form : {}", form);
        if (!"OK".equals(form.getMessage())) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }

        BigDecimal amount = form.getBiz_amt().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        CallBackFrom callBackFrom = CallBackFrom.builder()
                .transactionId(form.getSys_order_no())
                .orderId(form.getOrder_no())
                .amount(amount)
                .realAmount(amount)
                .note(form.getSign())
                .build();

        // 訂單支付成功
        if (Objects.equals(form.getStatus(), "true")) {
            orderSuccessProcess(callBackFrom);
            return "Success";
        }

        // 訂單支付失敗
        orderFailProcess(callBackFrom);
        return "Success";
    }

    /**
     * 用 POST 传递
     * 只会回调成功的订单
     * 回调成功 只需要回传纯文字 ok
     *
     * @param form
     * @return
     */
    public String callBackTB(CallBackTBForm form) {
        log.info("TB form : {}", form);

        CallBackFrom callBackFrom = CallBackFrom.builder()
                .transactionId(String.valueOf(form.getTrade_no()))
                .orderId(form.getOut_trade_no())
                .amount(form.getRequest_amount())
                .realAmount(form.getAmount())
                .note(form.getState())
                .build();

        // 訂單支付成功
        orderSuccessProcess(callBackFrom);

        return "ok";
    }

    public String callBackTBPay(CallBackTBBehalfPayForm form) {
        log.info("TB pay form : {}", form);

        MemberWithdraw memberWithdraw = iWithdrawService.lambdaQuery()
                .eq(MemberWithdraw::getWithdrawNo, form.getOut_trade_no())
                .eq(MemberWithdraw::getRemitState, RemitStateEnum.PAY_PENDING.getState())
                .oneOpt()
                .orElseThrow(() -> new BizException("TB pay 無此代付訂單"));

        // 代付訂單成功
        if (Objects.equals(form.getState(), "completed")) {
            log.info("TB Pay代付成功 success before {}", form);

            iWithdrawService.lambdaUpdate()
                    .eq(MemberWithdraw::getWithdrawNo, memberWithdraw.getWithdrawNo())
                    .set(MemberWithdraw::getRemitState, RemitStateEnum.PAY_SUCCESS.getState())
                    .set(MemberWithdraw::getRemitNote, "自動代付成功")
                    .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                    .update();

            log.info("TB Pay代付成功 success after {}", form);

            return "ok";
        }

        log.info("TB Pay代付成功 fail {}", form);

        return "fail";
    }

    public String callBackFB(CallBackFBForm form) {
        log.info("FB callBackFB form : {}", form);

        // amount	存款金额	String	需转化为2个小数点的字串
        // gateway	支付方式	String	渠道代码可查附录
        // status	订单状态	String	订单状态可查附录
        // merchant_order_num	订单号	String	商户自定义，唯一辨别订单的参数
        // merchant_order_remark	备注	String	商户自定义，允许空字串
        // sign	数据签名	String	遗留参数，3.1.1 后版本可无视

        Merchant merchant = iMerchantService.lambdaQuery()
                .eq(Merchant::getCode, FBPayConstant.MERCHANT_CODE)
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        JSONObject apiParameter = merchant.getApiParameter();
        String key = String.valueOf(apiParameter.get("key"));
        String iv = String.valueOf(apiParameter.get("iv"));

        if (FBPayConstant.SUCCESS.equals(form.getCode())) {
            // 訂單支付成功
            JSONObject orderJson = JSON.parseObject(AESUtil.decrypt(form.getOrder().replaceAll("\\n", ""), key, iv));
            Integer status = RECHARGE_STATE_MAP.getOrDefault(orderJson.getString("status"), 3);
            if (status == 1) {
                orderSuccessProcess(
                        CallBackFrom.builder()
                                .transactionId(String.valueOf(form.getMerchant_order_num()))
                                .orderId(form.getMerchant_order_num())
                                .amount(orderJson.getBigDecimal("amount"))
                                .realAmount(orderJson.getBigDecimal("amount"))
                                .note(orderJson.getString("merchant_order_remark"))
                                .build()
                );
                return FBPayConstant.SUCCESS;
            }
        }

        // 訂單支付失敗
        orderFailProcess(
                CallBackFrom.builder()
                        .orderId(form.getMerchant_order_num())
                        .build());

        return FBPayConstant.SUCCESS;
    }

    public String callBackFBPay(CallBackFBehalfPayForm form) {
        log.info("FB callBackFBPay form : {}", form);

        MemberWithdraw memberWithdraw = iWithdrawService.lambdaQuery()
                .eq(MemberWithdraw::getWithdrawNo, form.getMerchant_order_num())
                .eq(MemberWithdraw::getRemitState, RemitStateEnum.PAY_PENDING.getState())
                .oneOpt()
                .orElseThrow(() -> new BizException("FB pay 無此代付訂單"));

        // 代付訂單成功
        if (FBPayConstant.SUCCESS.equals(form.getCode())) {
            log.info("FB Pay代付成功 success before {}", form);

            iWithdrawService.lambdaUpdate()
                    .eq(MemberWithdraw::getWithdrawNo, memberWithdraw.getWithdrawNo())
                    .set(MemberWithdraw::getRemitState, RemitStateEnum.PAY_SUCCESS.getState())
                    .set(MemberWithdraw::getRemitNote, "自動代付成功")
                    .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                    .update();

            log.info("FB Pay代付成功 success after {}", form);

            return FBPayConstant.SUCCESS;
        }

        log.info("FB Pay代付失敗 fail {}", form);

        return FBPayConstant.SUCCESS;
    }

    public String callBackTR(CallBackTRForm form) {
        log.info("TR form : {}", form);

        CallBackFrom callBackFrom = CallBackFrom.builder()
                .transactionId(form.getTransaction_id())
                .orderId(form.getOrderid())
                .amount(form.getAmount())
                .realAmount(form.getAmount())
                .note(form.getAttach())
                .build();

        // 訂單支付成功
        if (Objects.equals(form.getReturncode(), "00")) {
            orderSuccessProcess(callBackFrom);
            return "OK";
        }
        log.info("TR Pay支付失敗 fail {}", form);

        return "fail";
    }

    public String callBackTRPay(CallBackTRBehalfPayForm form) {
        log.info("TR pay form : {}", form);

        MemberWithdraw memberWithdraw = iWithdrawService.lambdaQuery()
                .eq(MemberWithdraw::getWithdrawNo, form.getOrderid())
                .eq(MemberWithdraw::getRemitState, RemitStateEnum.PAY_PENDING.getState())
                .oneOpt()
                .orElseThrow(() -> new BizException("TR pay 無此代付訂單"));

        // 代付訂單
        if (Objects.equals(form.getRefCode(), "2")) {

            iWithdrawService.lambdaUpdate()
                    .eq(MemberWithdraw::getWithdrawNo, memberWithdraw.getWithdrawNo())
                    .set(MemberWithdraw::getRemitState, RemitStateEnum.PAY_SUCCESS.getState())
                    .set(MemberWithdraw::getRemitNote, "自動代付成功")
                    .set(MemberWithdraw::getRemitTime, LocalDateTime.now())
                    .update();

            log.info("TR Pay代付成功 success {}", form);
            return "OK";
        }

        log.info("TR Pay代付失敗 fail {}", form);

        return "fail";
    }

    public String callBackWuBa(CallBackWuBaForm form) {
        log.info("58 form : {}", form);
        CallBackFrom callBackFrom = CallBackFrom.builder()
                .transactionId(form.getOrder_id())
                .orderId(form.getOrder_id())
                .amount(form.getAmount())
                .realAmount(form.getAmount())
                .note(form.getSign())
                .build();

        if ("5".equals(form.getStatus())) {
            // 訂單支付成功
            orderSuccessProcess(callBackFrom);
            return SUCCESS_CALL_BACK;
        }

        // 訂單支付失敗
        orderFailProcess(callBackFrom);
        return SUCCESS_CALL_BACK;
    }

    public String callBackWuBaPay(CallBackWuBaPayForm form) {
        log.info("58 pay form : {}", form);
        MemberWithdraw memberWithdraw = iWithdrawService.lambdaQuery()
                .eq(MemberWithdraw::getWithdrawNo, form.getOrder_id())
                .eq(MemberWithdraw::getRemitState, RemitStateEnum.PAY_PENDING.getState())
                .oneOpt()
                .orElseThrow(() -> new BizException("58 pay 無此代付訂單"));

        LambdaUpdateChainWrapper<MemberWithdraw> memberWithdrawUpdateWrapper = iWithdrawService.lambdaUpdate()
                .eq(MemberWithdraw::getWithdrawNo, memberWithdraw.getWithdrawNo())
                .set(MemberWithdraw::getRemitTime, LocalDateTime.now());

        // 代付訂單成功
        if ("5".equals(form.getStatus())) {
            log.info("58 Pay代付成功 success before {}", form);
            memberWithdrawUpdateWrapper
                    .set(MemberWithdraw::getRemitState, RemitStateEnum.PAY_SUCCESS.getState())
                    .set(MemberWithdraw::getRemitNote, "自動代付成功");

        } else {
            log.info("58 Pay代付失敗 fail {}", form);
            memberWithdrawUpdateWrapper
                    .set(MemberWithdraw::getRemitState, RemitStateEnum.PAY_FAILED.getState())
                    .set(MemberWithdraw::getRemitNote, "自動代付失敗");
        }

        memberWithdrawUpdateWrapper.update();

        return SUCCESS_CALL_BACK;

    }
}

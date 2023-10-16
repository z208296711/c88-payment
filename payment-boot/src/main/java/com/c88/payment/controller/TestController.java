package com.c88.payment.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.c88.common.core.enums.EnableEnum;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.payment.pojo.dto.BaseBehalfPayDTO;
import com.c88.payment.pojo.dto.BasePayDTO;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.entity.MerchantRechargeChannel;
import com.c88.payment.pojo.form.CallBackGeneralForm;
import com.c88.payment.pojo.vo.CheckThirdPartyPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyBehalfPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyPaymentVO;
import com.c88.payment.service.IMerchantRechargeChannelService;
import com.c88.payment.service.IMerchantService;
import com.c88.payment.service.thirdparty.FengYangPayService;
import com.c88.payment.service.thirdparty.ICallBackService;
import com.c88.payment.service.thirdparty.IThirdPartPayService;
import com.c88.payment.service.thirdparty.ThirdPartPaymentExecutor;
import com.c88.payment.service.thirdparty.WPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
@Profile("!prod & !pre")
@Tag(name = "測試各項功能")
public class TestController {

    private final IMerchantService iMerchantService;
    private final IMerchantRechargeChannelService iMerchantRechargeChannelService;

    private final FengYangPayService fengYangPayService;

    private final ICallBackService fengYangCallBackImpl;
    private final ThirdPartPaymentExecutor thirdPartPaymentExecutor;

    private final WPayService wPayService;

    @Operation(summary = "建立支付訂單")
    @GetMapping("/createPayOrder/{merchantCode}/{channelId}")
    public Result<ThirdPartyPaymentVO> createPayOrder(@PathVariable("merchantCode") String merchantCode, @PathVariable("channelId") String channelId) {
        IThirdPartPayService payService = thirdPartPaymentExecutor.findByMerchantCode(merchantCode);
        Merchant merchant = iMerchantService.lambdaQuery()
                .eq(Merchant::getCode, merchantCode)
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        return payService.createPayOrder(merchant,
                BasePayDTO.builder()
                        .orderId(RandomUtil.randomString(16))
                        .amount(new BigDecimal("500"))
                        .build()
        );
    }

    @Operation(summary = "建立代付訂單")
    @GetMapping("/createBehalfPayOrder/{merchantCode}/{channelId}")
    public Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder(@PathVariable("merchantCode") String merchantCode, @PathVariable("channelId") String channelId) {
        IThirdPartPayService payService = thirdPartPaymentExecutor.findByMerchantCode(merchantCode);
        Merchant merchant = iMerchantService.lambdaQuery()
                .eq(Merchant::getCode, merchantCode)
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        return payService.createBehalfPayOrder(merchant,
                BaseBehalfPayDTO.builder()
                        .orderId(RandomUtil.randomString(16))
                        .bankCode("101")
                        .bankAccount(RandomUtil.randomString("abcdefghijklmnopqrstuvwxyz", 3).toUpperCase())
                        // .bankNo("6212262000000000001")
                        .bankNo("4556689773160486")
                        .amount(new BigDecimal("1"))
                        .build()
        );
    }

    @Operation(summary = "查詢訂單")
    @GetMapping("/checkPaymentStatus/{merchantCode}/{orderId}")
    public Result<CheckThirdPartyPaymentVO> checkPaymentStatus(@PathVariable("merchantCode") String merchantCode, @PathVariable("orderId") String orderId) {
        IThirdPartPayService payService = thirdPartPaymentExecutor.findByMerchantCode(merchantCode);
        Merchant merchant = iMerchantService.lambdaQuery()
                .eq(Merchant::getCode, merchantCode)
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        return payService.checkPaymentStatus(merchant, orderId);
    }

    @Operation(summary = "查詢代付單")
    @GetMapping("/checkWithdrawStatus/{merchantCode}/{orderId}")
    public Result<CheckThirdPartyPaymentVO> checkWithdrawStatus(@PathVariable("merchantCode") String merchantCode, @PathVariable("orderId") String orderId) {
        IThirdPartPayService payService = thirdPartPaymentExecutor.findByMerchantCode(merchantCode);
        Merchant merchant = iMerchantService.lambdaQuery()
                .eq(Merchant::getCode, merchantCode)
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        return payService.checkWithdrawStatus(merchant, orderId);
    }

    @Operation(summary = "查詢公司餘額")
    @GetMapping("/getCompanyBalance/{merchantCode}")
    public BigDecimal checkPaymentStatus(@PathVariable("merchantCode") String merchantCode) {
        IThirdPartPayService payService = thirdPartPaymentExecutor.findByMerchantCode(merchantCode);
        Merchant merchant = iMerchantService.lambdaQuery()
                .eq(Merchant::getCode, merchantCode)
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        return payService.getCompanyBalance(merchant);
    }

    @Operation(summary = "建立支付訂單")
    @GetMapping("/createPayOrder/test2/{merchantCode}/{channelId}")
    public Result<ThirdPartyPaymentVO> createPayOrder(HttpServletRequest request,
                                                      @PathVariable("merchantCode") String merchantCode,
                                                      @PathVariable("channelId") Integer channelId) {

        // 取得通道
        MerchantRechargeChannel merchantRechargeChannel = iMerchantRechargeChannelService.lambdaQuery()
                .eq(MerchantRechargeChannel::getMerchantId, 26)
                .eq(MerchantRechargeChannel::getRechargeTypeId, channelId)
                .eq(MerchantRechargeChannel::getEnable, EnableEnum.START.getCode())
                .eq(MerchantRechargeChannel::getBankId, 1)
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        Merchant merchant = iMerchantService.findByCode(merchantCode);

        IThirdPartPayService thirdPartPayService = thirdPartPaymentExecutor.findByMerchantCode(merchantCode);
        BasePayDTO basePayDTO = new BasePayDTO();
        basePayDTO.setOrderId(UUID.fastUUID().toString(false));

        basePayDTO.setAmount(BigDecimal.valueOf(1020));
        basePayDTO.setChannelId(merchantRechargeChannel.getParam());
        basePayDTO.setRechargeBankCode(merchantRechargeChannel.getRechargeBankCode());
        basePayDTO.setUserIp(ServletUtil.getClientIP(request));
        basePayDTO.setMemberUsername("garytest");
        basePayDTO.setMemberId(117l);
        Result<ThirdPartyPaymentVO> paymentVOResult = thirdPartPayService.createPayOrder(merchant, basePayDTO);

        log.info("data", paymentVOResult.getData());

        return null;
    }

    @Operation(summary = "建立鳳陽支付訂單")
    @GetMapping("/createFYPayOrder/{merchantId}/{channelId}")
    public Result<ThirdPartyPaymentVO> createFYPayOrder(@PathVariable("merchantId") Integer merchantId, @PathVariable("channelId") String channelId) {
        Merchant mId = iMerchantService.getById(merchantId);
        return fengYangPayService.createPayOrder(mId, BasePayDTO.builder()
                .orderId(channelId).userIp("35.187.204.224").channelId("907").amount(BigDecimal.valueOf(100000L)).build());

    }

    @Operation(summary = "鳳陽支付訂單回呼")
    @PostMapping(value = "/fyPayOrderCallBack", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String fyPayOrderCallBack(CallBackGeneralForm form) {
        fengYangCallBackImpl.callBack(form);
        return "";

    }

    @Operation(summary = "建立鳳陽代付訂單")
    @GetMapping("/createFYBehalfPayOrder/{merchantId}/{channelId}")
    public Result<ThirdPartyBehalfPaymentVO> createFYBehalfPayOrder(@PathVariable("merchantId") Integer merchantId, @PathVariable("channelId") String channelId) {
        Merchant mId = iMerchantService.getById(merchantId);
        return fengYangPayService.createBehalfPayOrder(mId, BaseBehalfPayDTO.builder()
                .userIp("35.187.204.224")
                .orderId(channelId)
                .amount(BigDecimal.valueOf(500000.00)).bankAccount("AABBCC").bankCity("Hanoi")
                .bankProvince("Tỉnh Bắc Ninh")
                .bankSub("Tỉnh Bắc Ninh").bankCode("2001")
                .bankNo("2001").build());
    }

    @Operation(summary = "建立WPAY支付訂單")
    @GetMapping("/createWPayOrder/{merchantId}")
    public Result<ThirdPartyPaymentVO> createWPayOrder(@PathVariable("merchantId") Integer merchantId) {
        Merchant mId = iMerchantService.getById(merchantId);
        return wPayService.createPayOrder(mId, BasePayDTO.builder().rechargeBankCode("gcash")
                .orderId("SF2023020160288874791").userIp("35.187.204.224").channelId("3").amount(BigDecimal.valueOf(100L)).build());

    }

    @Operation(summary = "建立WPAY代付訂單")
    @GetMapping("/createWPayBehalfOrder/{merchantId}")
    public Result<ThirdPartyBehalfPaymentVO> createWPayBehalfOrder(@PathVariable("merchantId") Integer merchantId) {
        Merchant mId = iMerchantService.getById(merchantId);
        return wPayService.createBehalfPayOrder(mId, BaseBehalfPayDTO.builder()
                .orderId("WD2023020160288874719").bankCode("gcash").bankAccount("Jimmmmy Chen").bankNo("A1234567")
                .amount(BigDecimal.valueOf(100L)).build());
    }



    @Operation(summary = "建立58支付訂單")
    @GetMapping("/createPayOrder/58/{merchantCode}/{channelId}")
    public Result<ThirdPartyPaymentVO> createPayOrder58(@PathVariable("merchantCode") String merchantCode, @PathVariable("channelId") String channelId) {
        IThirdPartPayService payService = thirdPartPaymentExecutor.findByMerchantCode(merchantCode);
        Merchant merchant = iMerchantService.lambdaQuery()
                .eq(Merchant::getCode, merchantCode)
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        return payService.createPayOrder(merchant,
                BasePayDTO.builder().channelId("3").rechargeBankCode("gcash")
                        .orderId(RandomUtil.randomString(16))
                        .amount(new BigDecimal("500"))
                        .build()
        );
    }

    @Operation(summary = "建立58代付訂單")
    @GetMapping("/createBehalfPayOrder/58/{merchantCode}/{channelId}")
    public Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder58(@PathVariable("merchantCode") String merchantCode, @PathVariable("channelId") String channelId) {
        IThirdPartPayService payService = thirdPartPaymentExecutor.findByMerchantCode(merchantCode);
        Merchant merchant = iMerchantService.lambdaQuery()
                .eq(Merchant::getCode, merchantCode)
                .oneOpt()
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

        return payService.createBehalfPayOrder(merchant,
                BaseBehalfPayDTO.builder()
                        .orderId(RandomUtil.randomString(16))
                        .bankCode("gcash")
                        .bankAccount(RandomUtil.randomString("abcdefghijklmnopqrstuvwxyz", 3).toUpperCase())
                         .bankNo("21541214121")
                        .bankNo("4556689773160486")
                        .amount(new BigDecimal("100"))
                        .build()
        );
    }

}

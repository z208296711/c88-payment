package com.c88.payment.controller;

import com.c88.payment.pojo.form.*;
import com.c88.payment.pojo.form.CallBackFBForm;
import com.c88.payment.pojo.form.CallBackGeneralForm;
import com.c88.payment.pojo.form.CallBackLidaForm;
import com.c88.payment.pojo.form.CallBackPrinceForm;
import com.c88.payment.pojo.form.CallBackFBehalfPayForm;
import com.c88.payment.pojo.form.CallBackTBForm;
import com.c88.payment.pojo.form.CallBackWuBaForm;
import com.c88.payment.pojo.form.CallBackWuBaPayForm;
import com.c88.payment.pojo.form.DipperPayCallBackForm;
import com.c88.payment.pojo.form.DipperPayOutCallBackForm;
import com.c88.payment.pojo.form.*;
import com.c88.payment.service.CallBackService;
import com.c88.payment.service.thirdparty.DipperCallBackServiceImpl;
import com.c88.payment.service.thirdparty.ICallBackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "『後台』三方回call", description = "https://{profile}-c88-callback.hyu.tw/payment/callBack/**")
@RequiredArgsConstructor
@RequestMapping("/callBack")
public class CallBackController {

    private final CallBackService callBackService;

    private final ICallBackService fengYangCallBackImpl;

    private final DipperCallBackServiceImpl dipperCallBackService;

    private final ICallBackService WPayCallBackImpl;

    private final ICallBackService WUBAPayCallBackImpl;

    @Operation(summary = "太子支付接口回調")
    @PostMapping(value = "/prince", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String callBackPrince(CallBackPrinceForm form) {
        return callBackService.callBackPrince(form);
    }

    @Operation(summary = "太子代付接口回調")
    @PostMapping(value = "/prince/pay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String callBackPrincePay(CallBackPrinceForm form) {
        return callBackService.callBackPrincePay(form);
    }

    @Operation(summary = "利達支付接口回調")
    @PostMapping(value = "/lida")
    public String callBackLida(@RequestBody CallBackLidaForm form) {
        return callBackService.callBackLida(form);
    }

    @Operation(summary = "鳳陽支付接口回調")
    @PostMapping(value = "/fengyang", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String callBackFengYang(CallBackGeneralForm form) {
        return fengYangCallBackImpl.callBack(form);
    }

    @Operation(summary = "鳳陽代付接口回調")
    @PostMapping(value = "/fengyang/pay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String callBackFengYangPay(CallBackGeneralForm form) {
        return fengYangCallBackImpl.callBackPay(form);
    }

    @Operation(summary = "北斗支付接口回調")
    @PostMapping(value = "/dipper", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String callDipperBackPrince(DipperPayCallBackForm form) {
        dipperCallBackService.callBack(form);
        return "SUCCESS";
    }

    @Operation(summary = "北斗代付接口回調")
    @PostMapping(value = "/dipper/pay", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String callDipperBackPrincePay(DipperPayOutCallBackForm form) {
        dipperCallBackService.callBackPay(form);
        return "SUCCESS";
    }

    @Operation(summary = "Wpay支付接口回調")
    @PostMapping(value = "/wpay", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String callBackWPay(CallBackGeneralForm form) {
        return WPayCallBackImpl.callBack(form);
    }

    @Operation(summary = "Wpay代付接口回調")
    @PostMapping(value = "/wpay/pay", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String callBackWPayPay(CallBackGeneralForm form) {
        return WPayCallBackImpl.callBackPay(form);
    }

    @Operation(summary = "TB支付接口回調")
    @PostMapping(value = "/tb")
    public String callBackTB(@RequestBody CallBackTBForm form) {
        return callBackService.callBackTB(form);
    }

    @Operation(summary = "TB代付接口回調")
    @PostMapping(value = "/tb/pay")
    public String callBackTBPay(@RequestBody CallBackTBBehalfPayForm form) {
        return callBackService.callBackTBPay(form);
    }

    @Operation(summary = "TR支付接口回調")
    @PostMapping(value = "/trpay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String callBackTR(CallBackTRForm form) {
        return callBackService.callBackTR(form);
    }

    @Operation(summary = "TR代付接口回調")
    @PostMapping(value = "/trpay/pay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String callBackTRPay(CallBackTRBehalfPayForm form) {
        return callBackService.callBackTRPay(form);
    }

    @Operation(summary = "FB支付接口回調")
    @PostMapping(value = "/fb", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String callBackFB( CallBackFBForm form) {
        return callBackService.callBackFB(form);
    }

    @Operation(summary = "FB代付接口回調")
    @PostMapping(value = "/fb/pay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String callBackFBPay( CallBackFBehalfPayForm form) {
        return callBackService.callBackFBPay(form);
    }

    @Operation(summary = "58支付接口回調")
    @PostMapping(value = "/wubaPay")
    public String callBackWUBAPay(CallBackWuBaForm form) {
        return callBackService.callBackWuBa(form);
    }

    @Operation(summary = "58代付接口回調")
    @PostMapping(value = "/wubaPay/pay")
    public String callBackWUBAPayPay(CallBackWuBaPayForm form) {
        return callBackService.callBackWuBaPay(form);
    }

}

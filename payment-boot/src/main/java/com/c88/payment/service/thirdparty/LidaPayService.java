package com.c88.payment.service.thirdparty;

import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.payment.pojo.dto.BaseBehalfPayDTO;
import com.c88.payment.pojo.dto.BasePayDTO;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.vo.CheckThirdPartyPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyBehalfPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyPaymentVO;
import com.c88.payment.service.thirdparty.utils.SftSignUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 利達
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LidaPayService implements IThirdPartPayService {

    private final RestTemplate restTemplate;

    private final String code = "lida";
    private final String responseSuccess = "success";
    private final String responseFail = "false";
    private final String signType = "MD5";
    // 訂單狀態
    private final Map<String, Integer> statusMap = Map.of(
            "true", 1,
            "processing", 2,
            "fail", 3
    );

    @Value("${spring.profiles.active}")
    public String active;

    @Override
    public Result<ThirdPartyPaymentVO> createPayOrder(Merchant merchant, BasePayDTO payDTO) {
        String url = merchant.getPayUrl() + "/pay";

        JSONObject apiParameter = merchant.getApiParameter();
        String merchantId = apiParameter.getString("merchantId");
        String key = apiParameter.getString("key");

        Map<String, String> req = new TreeMap<>();
        req.put("service", "PayCreateOrder");
        req.put("merchant_id", merchantId);
        req.put("biz_type", payDTO.getChannelId());
        req.put("nonce_str", payDTO.getOrderId());
        req.put("order_no", payDTO.getOrderId());
        req.put("biz_amt", payDTO.getAmount().stripTrailingZeros().toPlainString());
        req.put("notify_url", merchant.getNotify());
        if (Objects.nonNull(payDTO.getRechargeBankCode())) {
            req.put("bankCode", payDTO.getRechargeBankCode());
        }
        String signStr = SftSignUtil.formatSignData(req);
        String sign = "";
        try {
            sign = SftSignUtil.sign(signType, signStr, key, "LIDA");
        } catch (Exception e) {
            log.info("sign gen fail : {}", e);
        }
        req.put("sign", sign);

        log.info("=====createPayOrder url {} before {}", url, req);
        JSONObject responseJson = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====createPayOrder after {}", responseJson);

        if (responseFail.equals(responseJson.getString("result"))) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }

        ThirdPartyPaymentVO payVO = ThirdPartyPaymentVO.builder()
                .orderId(payDTO.getOrderId())
                .payUrl(responseJson.getString("url"))
                .amount(payDTO.getAmount())
                .build();

        return Result.success(payVO);
    }

    @Override
    public Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder(Merchant merchant, BaseBehalfPayDTO payDTO) {
        String url = merchant.getPayUrl() + "/pay";

        JSONObject apiParameter = merchant.getApiParameter();
        String merchantId = apiParameter.getString("merchantId");
        String key = apiParameter.getString("key");

        Map<String, String> req = new HashMap<>();
        req.put("service", "WithdrawalOrder");
        req.put("merchant_id", merchantId);
        req.put("biz_type", "105");// 利達個人銀行ID
        req.put("nonce_str", payDTO.getOrderId());
        req.put("order_no", payDTO.getOrderId());
        req.put("biz_amt", payDTO.getAmount().stripTrailingZeros().toPlainString());
        req.put("notify_url", merchant.getNotify());
        req.put("bankCode", payDTO.getBankCode());
        req.put("accName", payDTO.getBankAccount());
        req.put("cardNo", payDTO.getBankNo());
        String signStr = SftSignUtil.formatSignData(req);
        String sign = "";
        try {
            sign = SftSignUtil.sign(signType, signStr, key, "LIDA");
        } catch (Exception e) {
            log.info("sign gen fail : {}", e);
        }
        req.put("sign", sign);

        log.info("=====createBehalfPayOrder url {} before {}", url, req);
        JSONObject responseJson = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====createBehalfPayOrder after {}", responseJson);

        return Result.success(ThirdPartyBehalfPaymentVO.builder().build());
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkPaymentStatus(Merchant merchant, String orderId) {
        String url = merchant.getPayUrl() + "/pay";
        JSONObject apiParameter = merchant.getApiParameter();
        String merchantId = apiParameter.getString("merchantId");
        String key = apiParameter.getString("key");

        String signType = "MD5";

        Map<String, String> req = new TreeMap<>();
        req.put("service", "OrderQuery");
        req.put("merchant_id", merchantId);
        req.put("nonce_str", orderId);
        req.put("order_no", orderId);
        String signStr = SftSignUtil.formatSignData(req);
        String sign = "";
        try {
            sign = SftSignUtil.sign(signType, signStr, key, "LIDA");
        } catch (Exception e) {
            log.info("sign gen fail : {}", e);
        }
        req.put("sign", sign);
        log.info("=====checkPaymentStatus url {} after {}", url, req);
        JSONObject responseJson = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====checkPaymentStatus before {}", responseJson);

        if (responseFail.equals(responseJson.getString("result"))) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }

        CheckThirdPartyPaymentVO checkOrderVO = CheckThirdPartyPaymentVO.builder()
                .transactionId(responseJson.getString("sys_order_no"))
                .orderId(responseJson.getString("order_no"))
                .status(statusMap.getOrDefault(responseJson.getString("status"), 0))
                .amount(responseJson.getBigDecimal("biz_amt"))
                .build();

        return Result.success(checkOrderVO);
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkWithdrawStatus(Merchant merchant, String orderId) {
        String url = merchant.getPayUrl() + "/pay";
        JSONObject apiParameter = merchant.getApiParameter();
        String merchantId = apiParameter.getString("merchantId");
        String key = apiParameter.getString("key");

        String signType = "MD5";

        Map<String, String> req = new TreeMap<>();
        req.put("service", "OrderQuery");
        req.put("merchant_id", merchantId);
        req.put("nonce_str", orderId);
        req.put("order_no", orderId);
        String signStr = SftSignUtil.formatSignData(req);
        String sign = "";
        try {
            sign = SftSignUtil.sign(signType, signStr, key, "LIDA");
        } catch (Exception e) {
            log.info("sign gen fail : {}", e);
        }
        req.put("sign", sign);
        log.info("=====checkWithdrawStatus url {} after {}", url, req);
        JSONObject responseJson = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====checkWithdrawStatus before {}", responseJson);

        if (responseFail.equals(responseJson.getString("result"))) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }

        CheckThirdPartyPaymentVO checkOrderVO = CheckThirdPartyPaymentVO.builder()
                .transactionId(responseJson.getString("sys_order_no"))
                .orderId(responseJson.getString("order_no"))
                .status(statusMap.getOrDefault(responseJson.getString("status"), 0))
                .amount(responseJson.getBigDecimal("biz_amt"))
                .build();

        return Result.success(checkOrderVO);
    }

    @Override
    public Result<Boolean> setOrderExpired(String order, LocalDateTime localDateTime) {
        return null;
    }

    @Override
    public String getMerchantCode() {
        return code;
    }

    @Override
    public String encrypt(Map<String, Object> param, String key) {
        return null;
    }

    @Override
    public Boolean checkChannel(Merchant merchant, BasePayDTO payDTO) {
        String url = merchant.getPayUrl() + "/pay";
        payDTO.setAmount(BigDecimal.valueOf(50000));

        JSONObject apiParameter = merchant.getApiParameter();
        String merchantId = apiParameter.getString("merchantId");
        String key = apiParameter.getString("key");

        String signType = "MD5";

        Map<String, String> req = new TreeMap<>();
        req.put("service", "PayCreateOrder");
        req.put("merchant_id", merchantId);
        req.put("biz_type", payDTO.getChannelId());
        req.put("nonce_str", payDTO.getOrderId());
        req.put("order_no", payDTO.getOrderId());
        req.put("biz_amt", payDTO.getAmount().stripTrailingZeros().toPlainString());
        req.put("notify_url", merchant.getNotify());
        String signStr = SftSignUtil.formatSignData(req);
        String sign = "";
        try {
            sign = SftSignUtil.sign(signType, signStr, key, "LIDA");
        } catch (Exception e) {
            log.info("sign gen fail : {}", e);
        }
        req.put("sign", sign);

        JSONObject responseJson = restTemplate.postForObject(url, req, JSONObject.class);
        if (responseFail.equals(responseJson.getString("result"))) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    public BigDecimal getCompanyBalance(Merchant merchant) {
        // 判斷非正式環境取得固定餘額
        // if (!List.of("k8s_pre", "k8s_prod").contains(active)) {
        //     log.info("active {}", active);
        //     return BigDecimal.valueOf(1000000);
        // }

        List<String> channels = List.of("101", "102", "103", "104", "105");

        String url = merchant.getPayUrl() + "/pay";

        JSONObject apiParameter = merchant.getApiParameter();
        String merchantId = apiParameter.getString("merchantId");
        String key = apiParameter.getString("key");

        String signType = "MD5";

        // D0余额
        AtomicReference<BigDecimal> d0Balance = new AtomicReference<>(BigDecimal.ZERO);

        // D0冻结
        AtomicReference<BigDecimal> d0Freeze = new AtomicReference<>(BigDecimal.ZERO);

        // T1余额
        AtomicReference<BigDecimal> t1Balance = new AtomicReference<>(BigDecimal.ZERO);

        // T1冻结
        AtomicReference<BigDecimal> t1Freeze = new AtomicReference<>(BigDecimal.ZERO);

        Map<String, String> req = new TreeMap<>();
        req.put("service", "BalanceQuery");
        req.put("merchant_id", merchantId);
        channels.forEach(channel -> {
                    req.put("biz_type", channel);
                    String signStr = SftSignUtil.formatSignData(req);
                    String sign = "";
                    try {
                        sign = SftSignUtil.sign(signType, signStr, key, "LIDA");
                    } catch (Exception e) {
                        log.info("sign gen fail : {}", e);
                    }
                    req.put("sign", sign);

                    log.info("=====getCompanyBalance url {} before {}", url, req);
                    JSONObject responseJson = restTemplate.postForObject(url, req, JSONObject.class);
                    log.info("=====getCompanyBalance after {}", responseJson);

                    if (responseFail.equals(responseJson.getString("result"))) {
                        return;
                    }
                    JSONObject detailJson = responseJson.getJSONObject("detail");

                    d0Balance.set(detailJson.getBigDecimal("D0Balance"));
                    d0Freeze.set(detailJson.getBigDecimal("D0Freeze"));
                    t1Balance.set(detailJson.getBigDecimal("T1Balance"));
                    t1Freeze.set(detailJson.getBigDecimal("T1Freeze"));
                }
        );

        return d0Balance.get().add(t1Balance.get());
    }

}

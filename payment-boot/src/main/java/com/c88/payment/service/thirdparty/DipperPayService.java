package com.c88.payment.service.thirdparty;

import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.core.util.GameUtil;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 利達
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DipperPayService implements IThirdPartPayService {

    private final RestTemplate restTemplate;

    private final String code = "dipper";
    private final Integer SUCCESS = 1;
    private final String responseFail = "false";



    /** 北斗代付狀態
     * -2:上分失败
     * -1:建單失敗
     * 0:处理中
     * 1:上分成功
     * 3:无须上分
     * 9:API审核
     */
    // 訂單狀態
    private final Map<Integer, Integer> statusMap = Map.of(
            -2, 3,
            -1, 3,
            0, 2,
            1, 1,
            8, 1,
            9, 3
    );

    @Value("${spring.profiles.active}")
    public String active;

    @Override
    public Result<ThirdPartyPaymentVO> createPayOrder(Merchant merchant, BasePayDTO payDTO) {
        String url = merchant.getPayUrl() + "/pay/createOrder";
        JSONObject apiParameter = merchant.getApiParameter();
        String merNo = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        MultiValueMap<String, Object> req = new LinkedMultiValueMap<>();
        req.add("merNo", merNo);
        req.add("tradeNo", payDTO.getOrderId());
        req.add("cType", payDTO.getChannelId());
        req.add("bankCode", payDTO.getRechargeBankCode());
        req.add("orderAmount", payDTO.getAmount());
        req.add("playerId", payDTO.getMemberId());
        req.add("playerName", payDTO.getMemberUsername());
        req.add("notifyUrl", merchant.getNotify());
        req.add("sign", GameUtil.getMD5(merNo + payDTO.getOrderId() + payDTO.getAmount() + key));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(req, headers);


        log.info("=====createPayOrder url {} after {}", url, req);
        JSONObject response = restTemplate.postForObject(url, request, JSONObject.class);
        log.info("=====createPayOrder before {}", response);

        if (!SUCCESS.equals(response.getInteger("Success"))) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }

        ThirdPartyPaymentVO payVO = ThirdPartyPaymentVO.builder()
                .orderId(payDTO.getOrderId())
                .payUrl(response.getString("PayPage"))
                .amount(payDTO.getAmount())
                .build();

        return Result.success(payVO);
    }

    @Override
    public Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder(Merchant merchant, BaseBehalfPayDTO param) {
        String url = merchant.getPayUrl() + "/payout/createOrder";
        JSONObject apiParameter = merchant.getApiParameter();
        String merNo = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        MultiValueMap<String, Object> req = new LinkedMultiValueMap<>();
        req.add("merNo", merNo);
        req.add("tradeNo", param.getOrderId());
        req.add("cType", "Payout");
        req.add("bankCode", param.getBankCode());
        req.add("orderAmount", param.getAmount());
        req.add("accountName", param.getBankAccount());
        req.add("notifyUrl", merchant.getBehalfNotify());
        req.add("sign", GameUtil.getMD5(merNo + param.getOrderId() + param.getBankCode() + param.getAmount() + key));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(req, headers);


        log.info("=====createPayOrder url {} after {}", url, req);
        JSONObject response = restTemplate.postForObject(url, request, JSONObject.class);
        log.info("=====createPayOrder before {}", response);

        if (!SUCCESS.equals(response.getInteger("Success"))) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }

        return Result.success(ThirdPartyBehalfPaymentVO.builder().transactionId(param.getOrderId()).build());
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkPaymentStatus(Merchant merchant, String orderId) {
        String url = merchant.getPayUrl() + "/inquiry/payOrder";
        JSONObject apiParameter = merchant.getApiParameter();
        String merNo = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> req = new LinkedMultiValueMap<>();
        req.add("merNo", merNo);
        req.add("tradeNo", orderId);
        req.add("sign", GameUtil.getMD5(merNo + orderId + key));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(req, headers);


        log.info("=====checkPaymentStatus url {} after {}", url, req);
        JSONObject response = restTemplate.postForObject(url, request, JSONObject.class);
        log.info("=====checkPaymentStatus before {}", response);

        if (!SUCCESS.equals(response.getInteger("Success"))) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }

        CheckThirdPartyPaymentVO checkOrderVO = CheckThirdPartyPaymentVO.builder()
                .transactionId("")
                .orderId(orderId)
                .status(statusMap.getOrDefault(response.getString("tradeStatus"), 0))
                .amount(response.getBigDecimal("tradeStatus"))
                .build();

        return Result.success(checkOrderVO);
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkWithdrawStatus(Merchant merchant, String orderId) {
        String url = merchant.getPayUrl() + "/inquiry/payOrder";
        JSONObject apiParameter = merchant.getApiParameter();
        String merNo = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> req = new LinkedMultiValueMap<>();
        req.add("merNo", merNo);
        req.add("tradeNo", orderId);
        req.add("sign", GameUtil.getMD5(merNo + orderId + key));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(req, headers);


        log.info("=====checkWithdrawStatus url {} after {}", url, req);
        JSONObject response = restTemplate.postForObject(url, request, JSONObject.class);
        log.info("=====checkWithdrawStatus before {}", response);

        if (!SUCCESS.equals(response.getInteger("Success"))) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }

        CheckThirdPartyPaymentVO checkOrderVO = CheckThirdPartyPaymentVO.builder()
                .transactionId("")
                .orderId(orderId)
                .status(statusMap.getOrDefault(response.getString("tradeStatus"), 0))
                .amount(response.getBigDecimal("tradeStatus"))
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
            sign = SftSignUtil.sign(signType, signStr, key, "DIPPER");
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
                        sign = SftSignUtil.sign(signType, signStr, key, "DIPPER");
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

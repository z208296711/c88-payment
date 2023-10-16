package com.c88.payment.service.thirdparty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.core.util.AESUtil;
import com.c88.payment.pojo.dto.BaseBehalfPayDTO;
import com.c88.payment.pojo.dto.BasePayDTO;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.vo.CheckThirdPartyPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyBehalfPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyPaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.c88.payment.constants.Third.FBPayConstant.MERCHANT_CODE;
import static com.c88.payment.constants.Third.FBPayConstant.RECHARGE_STATE_MAP;
import static com.c88.payment.constants.Third.FBPayConstant.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class FBPayService implements IThirdPartPayService {

    private final RestTemplate restTemplate;

    @Override
    public String getMerchantCode() {
        return MERCHANT_CODE;
    }

    @Override
    public Result<ThirdPartyPaymentVO> createPayOrder(Merchant merchant, BasePayDTO payDTO) {
        String url = merchant.getPayUrl() + "/deposit";
        JSONObject apiParameter = merchant.getApiParameter();
        String merchantCode = String.valueOf(apiParameter.get("merchantCode"));// 商戶代碼
        String key = String.valueOf(apiParameter.get("key"));
        String iv = String.valueOf(apiParameter.get("iv"));

        // http格式化callbackUrl
        String notifyUrl;
        try {
            notifyUrl = URLEncoder.encode(merchant.getNotify(), "UTF-8");
        } catch (Exception e) {
            log.error("fb notify url encode fail");
            return Result.failed(ResultCode.SYSTEM_RESOURCE_ERROR);
        }

        Map<String, Object> param = new TreeMap<>();
        param.put("gateway", payDTO.getChannelId());
        param.put("amount", payDTO.getAmount().stripTrailingZeros().toPlainString());
        param.put("device", "mobile");// desktop / mobile（填入客户使用的平台，手机端请填入mobile）
        param.put("callback_url", notifyUrl);
        param.put("merchant_order_time", String.valueOf(Instant.now().getEpochSecond()));
        param.put("merchant_order_num", payDTO.getOrderId());
        param.put("merchant_order_remark", "");
        param.put("uid", payDTO.getMemberId());
        param.put("user_ip", payDTO.getUserIp());
        if (StringUtils.isNotBlank(payDTO.getRechargeBankCode())) {
            param.put("bank_code", payDTO.getRechargeBankCode());
        }

        // 取得編碼後的值
        String encrypt = AESUtil.encrypt(JSON.toJSONString(param), key, iv);
        JSONObject requestJson = new JSONObject();

        requestJson.put("merchant_slug", merchantCode);
        requestJson.put("data", encrypt);
        log.info("=====createPayOrder url {} before {}", url, requestJson);
        JSONObject jsonResult = restTemplate.postForObject(url, requestJson, JSONObject.class);
        log.info("=====createPayOrder after {}", jsonResult);

        String code = jsonResult.getString("code");
        if (!SUCCESS.equals(code)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    String.format("code : %s\tmessage : %s",
                            code,
                            jsonResult.getString("msg"))
            );
        }
        JSONObject orderJson = JSON.parseObject(AESUtil.decrypt(jsonResult.getString("order").replaceAll("\\n", ""), key, iv));

        return Result.success(
                ThirdPartyPaymentVO.builder()
                        .thirdPartyOrderId(orderJson.getString("merchant_order_num"))
                        .payUrl(orderJson.getString("navigate_url"))
                        .amount(orderJson.getBigDecimal("amount"))
                        .build()
        );
    }

    @Override
    public Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder(Merchant merchant, BaseBehalfPayDTO behalfPayDTO) {
        String url = merchant.getPayUrl() + "/withdraw";

        JSONObject apiParameter = merchant.getApiParameter();
        String merchantCode = String.valueOf(apiParameter.get("merchantCode"));// 商戶代碼
        String key = String.valueOf(apiParameter.get("key"));
        String iv = String.valueOf(apiParameter.get("iv"));

        // 編譯callbackUrl
        String notifyUrl;
        try {
            notifyUrl = URLEncoder.encode(merchant.getBehalfNotify(), "UTF-8");
        } catch (Exception e) {
            log.error("fb notify url encode fail");
            return Result.failed(ResultCode.SYSTEM_RESOURCE_ERROR);
        }

        Map<String, Object> param = new TreeMap<>();
        param.put("gateway", "gcash");// 暫時綁定GCash
        param.put("uid", behalfPayDTO.getMemberId());
        param.put("amount", behalfPayDTO.getAmount().stripTrailingZeros().toPlainString());
        param.put("callback_url", notifyUrl);
        param.put("merchant_order_num", behalfPayDTO.getOrderId());
        param.put("merchant_order_time", String.valueOf(Instant.now().getEpochSecond()));
        param.put("merchant_order_remark", "");
        param.put("user_ip", behalfPayDTO.getUserIp());
        param.put("bank_code", "UBP");
        param.put("card_number", behalfPayDTO.getBankNo());
        param.put("card_holder", behalfPayDTO.getBankAccount());

        // 取得編碼後的值
        String encrypt = AESUtil.encrypt(JSON.toJSONString(param), key, iv);
        JSONObject requestJson = new JSONObject();

        requestJson.put("merchant_slug", merchantCode);
        requestJson.put("data", encrypt);

        log.info("=====createBehalfPayOrder url {} before {}", url, requestJson);
        JSONObject jsonResult = restTemplate.postForObject(url, requestJson, JSONObject.class);
        log.info("=====createBehalfPayOrder after {}", jsonResult);

        String code = jsonResult.getString("code");
        if (!SUCCESS.equals(code)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    String.format("code : %s\tmessage : %s",
                            code,
                            jsonResult.getString("msg"))
            );
        }
        JSONObject orderJson = JSON.parseObject(AESUtil.decrypt(jsonResult.getString("order").replaceAll("\\n", ""), key, iv));

        String transactionId = orderJson.getString("sign");

        return Result.success(ThirdPartyBehalfPaymentVO.builder().transactionId(transactionId).build());
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkPaymentStatus(Merchant merchant, String orderId) {
        String url = merchant.getPayUrl() + "/check";

        JSONObject apiParameter = merchant.getApiParameter();
        String merchantCode = String.valueOf(apiParameter.get("merchantCode"));// 商戶代碼
        String key = String.valueOf(apiParameter.get("key"));
        String iv = String.valueOf(apiParameter.get("iv"));

        Map<String, Object> param = new TreeMap<>();
        param.put("merchant_order_num", orderId);

        // 取得編碼後的值
        String encrypt = AESUtil.encrypt(JSON.toJSONString(param), key, iv);
        JSONObject requestJson = new JSONObject();

        requestJson.put("merchant_slug", merchantCode);
        requestJson.put("data", encrypt);

        log.info("=====checkPaymentStatus url {} before {}", url, param);
        JSONObject responseJson = restTemplate.postForObject(url, requestJson, JSONObject.class);
        log.info("=====checkPaymentStatus after {}", responseJson);

        String code = responseJson.getString("code");
        if (!SUCCESS.equals(code)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    String.format("code : %s\tmessage : %s",
                            code,
                            responseJson.getString("msg"))
            );
        }

        JSONObject orderJson = JSON.parseObject(AESUtil.decrypt(responseJson.getString("order").replaceAll("\\n", ""), key, iv));

        return Result.success(
                CheckThirdPartyPaymentVO.builder()
                        .transactionId(orderJson.getString("sign"))
                        .orderId(orderJson.getString("merchant_order_num"))
                        .amount(orderJson.getBigDecimal("amount"))
                        .realAmount(orderJson.getBigDecimal("amount"))
                        .status(RECHARGE_STATE_MAP.get(orderJson.getString("status")))
                        .build()
        );
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkWithdrawStatus(Merchant merchant, String orderId) {
        String url = merchant.getPayUrl() + "/check";

        JSONObject apiParameter = merchant.getApiParameter();
        String merchantCode = String.valueOf(apiParameter.get("merchantCode"));// 商戶代碼
        String key = String.valueOf(apiParameter.get("key"));
        String iv = String.valueOf(apiParameter.get("iv"));

        Map<String, Object> param = new TreeMap<>();
        param.put("merchant_order_num", orderId);

        // 取得編碼後的值
        String encrypt = AESUtil.encrypt(JSON.toJSONString(param), key, iv);
        JSONObject requestJson = new JSONObject();

        requestJson.put("merchant_slug", merchantCode);
        requestJson.put("data", encrypt);

        log.info("=====checkWithdrawStatus url {} before {}", url, param);
        JSONObject responseJson = restTemplate.postForObject(url, requestJson, JSONObject.class);
        log.info("=====checkWithdrawStatus after {}", responseJson);

        String code = responseJson.getString("code");
        if (!SUCCESS.equals(code)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    String.format("code : %s\tmessage : %s",
                            code,
                            responseJson.getString("msg"))
            );
        }

        JSONObject orderJson = JSON.parseObject(AESUtil.decrypt(responseJson.getString("order").replaceAll("\\n", ""), key, iv));

        return Result.success(
                CheckThirdPartyPaymentVO.builder()
                        .transactionId(orderJson.getString("sign"))
                        .orderId(orderJson.getString("merchant_order_num"))
                        .amount(orderJson.getBigDecimal("amount"))
                        .realAmount(orderJson.getBigDecimal("amount"))
                        .status(RECHARGE_STATE_MAP.get(orderJson.getString("status")))
                        .build()
        );
    }

    @Override
    public Result<Boolean> setOrderExpired(String order, LocalDateTime localDateTime) {
        return null;
    }

    @Override
    public String encrypt(Map<String, Object> param, String key) {
        return null;
    }

    @Override
    public Boolean checkChannel(Merchant merchant, BasePayDTO payDTO) {
        String url = merchant.getPayUrl() + "/deposit";
        JSONObject apiParameter = merchant.getApiParameter();
        String merchantCode = String.valueOf(apiParameter.get("merchantCode"));// 商戶代碼
        String key = String.valueOf(apiParameter.get("key"));
        String iv = String.valueOf(apiParameter.get("iv"));

        // http格式化callbackUrl
        String notifyUrl;
        try {
            notifyUrl = URLEncoder.encode(merchant.getNotify(), "UTF-8");
        } catch (Exception e) {
            log.error("fb notify url encode fail by checkChannel");
            return Boolean.FALSE;
        }

        Map<String, Object> param = new TreeMap<>();
        param.put("gateway", payDTO.getChannelId());
        param.put("amount", new BigDecimal("500"));
        param.put("device", "mobile");// desktop / mobile（填入客户使用的平台，手机端请填入mobile）
        param.put("callback_url", notifyUrl);
        param.put("merchant_order_time", String.valueOf(Instant.now().getEpochSecond()));
        param.put("merchant_order_num", payDTO.getOrderId());
        param.put("merchant_order_remark", "");
        param.put("uid", payDTO.getMemberId());
        param.put("user_ip", payDTO.getUserIp());
        if (StringUtils.isNotBlank(payDTO.getRechargeBankCode())) {
            param.put("bank_code", payDTO.getRechargeBankCode());
        }

        // 取得編碼後的值
        String encrypt = AESUtil.encrypt(JSON.toJSONString(param), key, iv);
        JSONObject requestJson = new JSONObject();

        requestJson.put("merchant_slug", merchantCode);
        requestJson.put("data", encrypt);
        log.info("=====checkChannel url {} before {}", url, requestJson);
        JSONObject responseJson = restTemplate.postForObject(url, requestJson, JSONObject.class);
        log.info("=====checkChannel after {}", responseJson);

        String code = responseJson.getString("code");
        if (!SUCCESS.equals(code)) {
            log.error("fb pay checkChannel fail : {}",
                    String.format("code : %s\tmessage : %s",
                            code,
                            responseJson.getString("msg")));
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    public BigDecimal getCompanyBalance(Merchant merchant) {
        String url = merchant.getPayUrl() + "/balance";

        JSONObject apiParameter = merchant.getApiParameter();
        String merchantCode = String.valueOf(apiParameter.get("merchantCode"));// 商戶代碼
        String key = String.valueOf(apiParameter.get("key"));
        String iv = String.valueOf(apiParameter.get("iv"));

        Map<String, Object> param = new TreeMap<>();
        param.put("gateways", List.of("gcash"));// 暫時綁定gcash 不輸入取得全部錢包
        param.put("merchant_time", String.valueOf(Instant.now().getEpochSecond()));

        // 取得編碼後的值
        String encrypt = AESUtil.encrypt(JSON.toJSONString(param), key, iv);
        JSONObject requestJson = new JSONObject();

        requestJson.put("merchant_slug", merchantCode);
        requestJson.put("data", encrypt);

        log.info("=====getCompanyBalance url {} before {}", url, requestJson);
        JSONObject responseJson = restTemplate.postForObject(url, requestJson, JSONObject.class);
        log.info("=====getCompanyBalance after {}", responseJson);

        String code = responseJson.getString("code");
        if (!SUCCESS.equals(code)) {
            log.error("fb pay getCompanyBalance fail : {}",
                    String.format("code : %s\tmessage : %s",
                            code,
                            responseJson.getString("msg")));
            return BigDecimal.ZERO;
        }
        Map<String, BigDecimal> wallets = responseJson.getObject("wallets", new TypeReference<HashMap<String, BigDecimal>>() {
        });

        return wallets.getOrDefault("gcash", BigDecimal.ZERO);
    }

}

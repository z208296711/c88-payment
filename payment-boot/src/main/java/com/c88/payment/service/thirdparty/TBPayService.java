package com.c88.payment.service.thirdparty;

import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.payment.enums.ChannelRechargeCodeEnum;
import com.c88.payment.pojo.dto.BaseBehalfPayDTO;
import com.c88.payment.pojo.dto.BasePayDTO;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.vo.CheckThirdPartyPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyBehalfPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyPaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TBPayService implements IThirdPartPayService {

    private final RestTemplate restTemplate;

    private static final String MERCHANT_CODE = "tbpay";

    private static final Boolean FAIL = Boolean.FALSE;

    private static final String PARAM_QRCODE_API_TOKEN_STR = "qrcodeApiToken";
    private static final String PARAM_G_CASH_API_TOKEN_STR = "gCashApiToken";
    private static final String PARAM_MAYA_API_TOKEN_STR = "mayaApiToken";

    private static final String PARAM_QRCODE_NOTIFY_TOKEN_STR = "qrcodeNotifyToken";
    private static final String PARAM_G_CASH_NOTIFY_TOKEN_STR = "gCashNotifyToken";
    private static final String PARAM_MAYA_NOTIFY_TOKEN_STR = "mayaNotifyToken";

    private static final Map<String, Integer> stateMap = Map.of(
            "new", 0,
            "processing", 2,
            "verify", 2,
            "reject", 3,
            "completed", 1,
            "failed", 3,
            "refund", 6
    );

    @Value("${spring.profiles.active}")
    public String active;

    @Override
    public String getMerchantCode() {
        return MERCHANT_CODE;
    }

    @Override
    public Result<ThirdPartyPaymentVO> createPayOrder(Merchant merchant, BasePayDTO payDTO) {
        String url = merchant.getPayUrl() + "/transaction";

        JSONObject apiParameter = merchant.getApiParameter();

        String token = "";
        switch (ChannelRechargeCodeEnum.getEnum(payDTO.getRechargeBankCode())) {
            case G_CASH:
                token = apiParameter.getString(PARAM_G_CASH_API_TOKEN_STR);
                break;
            case MAYA:
                token = apiParameter.getString(PARAM_MAYA_API_TOKEN_STR);
                break;
            case QRCODE:
                token = apiParameter.getString(PARAM_QRCODE_API_TOKEN_STR);
                break;
        }

        JSONObject requestJson = new JSONObject();
        requestJson.put("amount", payDTO.getAmount());
        requestJson.put("out_trade_no", payDTO.getOrderId());
        requestJson.put("callback_url", merchant.getNotify());

        HttpEntity<JSONObject> entity = this.generateHttpEntity(requestJson, token);

        log.info("=====createPayOrder url {} before {}", url, entity);
        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        log.info("=====createPayOrder after {}", jsonResult);

        Boolean isSuccess = jsonResult.getBoolean("success");
        if (FAIL.equals(isSuccess)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    String.format("code : %s\tmessage : %s\terrors : %s",
                            jsonResult.getString("status_code"),
                            jsonResult.getString("message"),
                            jsonResult.getString("errors"))
            );
        }

        JSONObject dataJson = jsonResult.getJSONObject("data");

        return Result.success(
                ThirdPartyPaymentVO.builder()
                        .orderId(dataJson.getString("out_trade_no"))
                        .thirdPartyOrderId(dataJson.getString("trade_no"))
                        .payUrl(dataJson.getString("uri"))
                        .amount(dataJson.getBigDecimal("amount"))
                        .build()
        );
    }

    @Override
    public Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder(Merchant merchant, BaseBehalfPayDTO param) {
        String url = merchant.getPayUrl() + "/payment";

        JSONObject apiParameter = merchant.getApiParameter();

        JSONObject requestJson = new JSONObject();
        requestJson.put("out_trade_no", param.getOrderId());
        requestJson.put("bank_id", "GCASH");
        requestJson.put("bank_owner", param.getBankAccount());
        requestJson.put("account_number", param.getBankNo());
        requestJson.put("amount", param.getAmount().stripTrailingZeros().toPlainString());
        requestJson.put("callback_url", merchant.getBehalfNotify());
        requestJson.put("sign", this.genSign(requestJson, apiParameter.getString(PARAM_G_CASH_API_TOKEN_STR), apiParameter.getString(PARAM_G_CASH_NOTIFY_TOKEN_STR)));
        HttpEntity<JSONObject> entity = generateHttpEntity(requestJson, apiParameter.getString(PARAM_G_CASH_API_TOKEN_STR));

        log.info("=====createBehalfPayOrder url {} before {}", url, entity);
        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        log.info("=====createBehalfPayOrder after {}", jsonResult);

        Boolean isSuccess = jsonResult.getBoolean("success");

        // state狀態碼
        // ew => 新订单
        // processing => 处理中
        // reject => 拒绝
        // completed => 成功
        // failed => 失败
        // refund => 冲回
        if (FAIL.equals(isSuccess)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    String.format("stateCode : %s message : %s",
                            jsonResult.getString("status_code"),
                            jsonResult.getString("message")
                    )
            );
        }

        JSONObject data = jsonResult.getJSONObject("data");

        String transactionId = data.getString("trade_no");

        return Result.success(ThirdPartyBehalfPaymentVO.builder().transactionId(transactionId).build());
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkPaymentStatus(Merchant merchant, String orderId) {
        String url = merchant.getPayUrl() + "/transaction/" + orderId;

        JSONObject apiParameter = merchant.getApiParameter();

        HttpEntity<JSONObject> entity = generateHttpEntity(null, apiParameter.getString(PARAM_G_CASH_API_TOKEN_STR));

        log.info("=====checkPaymentStatus url {} before {}", url, entity);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, JSONObject.class);
        log.info("=====checkPaymentStatus after {}", responseEntity);

        if (responseEntity.getStatusCodeValue() != 200) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }

        JSONObject jsonResult = responseEntity.getBody();

        // 状态码
        // new => 新订单
        // processing => 处理中
        // verify => 待確認
        // reject => 拒绝
        // completed => 成功
        // failed => 失败
        // refund => 冲回
        Boolean isSuccess = jsonResult.getBoolean("success");
        if (FAIL.equals(isSuccess)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    String.format("state : %s",
                            jsonResult.getJSONObject("data").getString("state")
                    )
            );
        }

        JSONObject data = jsonResult.getJSONObject("data");

        return Result.success(
                CheckThirdPartyPaymentVO.builder()
                        .transactionId(data.getString("trade_no"))
                        .orderId(data.getString("out_trade_no"))
                        .amount(data.getBigDecimal("request_amount"))
                        .realAmount(data.getBigDecimal("amount"))
                        .status(stateMap.get(data.getString("state")))
                        .build()
        );
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkWithdrawStatus(Merchant merchant, String orderId) {
        String url = merchant.getPayUrl() + "/transaction/" + orderId;

        JSONObject apiParameter = merchant.getApiParameter();

        HttpEntity<JSONObject> entity = generateHttpEntity(null, apiParameter.getString(PARAM_G_CASH_API_TOKEN_STR));

        log.info("=====checkWithdrawStatus url {} before {}", url, entity);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, JSONObject.class);
        log.info("=====checkWithdrawStatus after {}", responseEntity);

        if (responseEntity.getStatusCodeValue() != 200) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }

        JSONObject jsonResult = responseEntity.getBody();

        // 状态码
        // new => 新订单
        // processing => 处理中
        // verify => 待確認
        // reject => 拒绝
        // completed => 成功
        // failed => 失败
        // refund => 冲回
        Boolean isSuccess = jsonResult.getBoolean("success");
        if (FAIL.equals(isSuccess)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    String.format("state : %s",
                            jsonResult.getJSONObject("data").getString("state")
                    )
            );
        }

        JSONObject data = jsonResult.getJSONObject("data");

        return Result.success(
                CheckThirdPartyPaymentVO.builder()
                        .transactionId(data.getString("trade_no"))
                        .orderId(data.getString("out_trade_no"))
                        .amount(data.getBigDecimal("request_amount"))
                        .realAmount(data.getBigDecimal("amount"))
                        .status(stateMap.get(data.getString("state")))
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
        // payDTO.setAmount(BigDecimal.valueOf(50000));
        // String response = restTemplate.postForObject(merchant.getPayUrl() + "/pay", generatePayReq(payDTO, merchant), String.class);
        // JSONObject jsonResult = JSON.parseObject(response);
        // Integer status = jsonResult.getInteger("status");
        // log.info(response);
        // return Objects.equals(SUCCESS, status);
        return null;
    }

    @Override
    public BigDecimal getCompanyBalance(Merchant merchant) {
        String url = merchant.getPayUrl() + "/balance/inquiry";

        JSONObject apiParameter = merchant.getApiParameter();

        HttpEntity<JSONObject> entity = generateHttpEntity(null, apiParameter.getString(PARAM_G_CASH_API_TOKEN_STR));

        log.info("=====getCompanyBalance url {} before {}", url, entity);
        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        log.info("=====getCompanyBalance after {}", jsonResult);

        Boolean isSuccess = jsonResult.getBoolean("success");

        if (FAIL.equals(isSuccess)) {
            log.info("=====getCompanyBalance fail {}", jsonResult);
            return BigDecimal.ZERO;
        }

        return jsonResult.getJSONObject("data").getBigDecimal("balance");
    }

    /**
     * 生成請求參數
     *
     * @param requestJson 請求參數
     * @param token       api令牌
     * @return
     */
    private HttpEntity<JSONObject> generateHttpEntity(JSONObject requestJson, String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add("Authorization", "Bearer " + token);
        return new HttpEntity<>(requestJson, httpHeaders);
    }

    /**
     * 生成簽名
     *
     * @param jsonObject
     * @param apiToken
     * @param notifyToken
     * @return
     */
    private String genSign(JSONObject jsonObject, String apiToken, String notifyToken) {

        //利用treeMap做排序
        Map<String, Object> treeMap = new TreeMap<>(jsonObject);

        StringJoiner sj = new StringJoiner("&");
        treeMap.forEach((key, value) -> {
            sj.add(String.format("%s=%s", key, value));
        });

        return MD5.create().digestHex(sj + apiToken + notifyToken);

    }

}

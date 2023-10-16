package com.c88.payment.service.thirdparty;

import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.core.util.DateUtil;
import com.c88.payment.enums.OrderStatus;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TRPayService implements IThirdPartPayService {

    private final RestTemplate restTemplate;

    private static final String MERCHANT_CODE = "trpay";

    private static final String signType = "MD5";

    @Value("${spring.profiles.active}")
    public String active;

    @Override
    public String getMerchantCode() {
        return MERCHANT_CODE;
    }

    @Override
    public Result<ThirdPartyPaymentVO> createPayOrder(Merchant merchant, BasePayDTO payDTO) {
        String url = merchant.getPayUrl() + "/Pay_Index.html";

        JSONObject apiParameter = merchant.getApiParameter();
        String key = apiParameter.getString("key");
        MultiValueMap params = new LinkedMultiValueMap();
        params.add("pay_memberid", apiParameter.getString("memberId"));
        params.add("pay_orderid", payDTO.getOrderId());
        params.add("pay_applydate", DateUtil.dateToStr(LocalDateTime.now(), DateUtil.ymdhms));
        params.add("pay_bankcode", "2");
        params.add("pay_notifyurl", merchant.getNotify());
        params.add("pay_callbackurl", merchant.getBehalfNotify());
        params.add("pay_amount", payDTO.getAmount().stripTrailingZeros().toPlainString());
        params.add("pay_userid", String.valueOf(payDTO.getMemberId()));
        params.add("pay_username", payDTO.getMemberUsername());
        params.add("pay_userphone", payDTO.getMemberPhone());
        params.add("pay_userip", payDTO.getUserIp());

        String signStr = SftSignUtil.formatSignData(params.toSingleValueMap());
        String sign;
        try {
            sign = SftSignUtil.sign(signType, signStr, key, "TR");
        } catch (Exception e) {
            log.info("sign gen fail : {}", e);
            return Result.failed();
        }
        params.add("pay_productname", "測試測試~~~");
        params.add("pay_md5sign", sign.toUpperCase());
        params.add("format", "json");
        HttpEntity<MultiValueMap> entity = generateHttpEntity(params, key);

        log.info("=====createPayOrder url {} before {}", url, params);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entity, String.class);
        log.info("=====createPayOrder after {}", responseEntity);
        JSONObject jsonResult = JSONObject.parseObject(responseEntity.getBody());
        String resultStatus = jsonResult.getString("status");
        if ("error".equals(resultStatus)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    jsonResult.getString("msg")
            );
        }

        String payUrl = jsonResult.getString("data");
        return Result.success(
                ThirdPartyPaymentVO.builder()
                        .orderId(payDTO.getOrderId())
                        .thirdPartyOrderId(null)
                        .payUrl(payUrl)
                        .amount(payDTO.getAmount().stripTrailingZeros())
                        .build()
        );
    }

    @Override
    public Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder(Merchant merchant, BaseBehalfPayDTO param) {
        String url = merchant.getPayUrl() + "/Payment_Dfpay_add.html";

        JSONObject apiParameter = merchant.getApiParameter();
        String key = apiParameter.getString("key");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("mchid", apiParameter.getString("memberId"));
        params.add("out_trade_no", param.getOrderId());
        params.add("money", param.getAmount().stripTrailingZeros().toPlainString());
        params.add("bankname", "菲律賓首都銀行");
        params.add("subbranch", "台北分行");
        params.add("accountname", param.getBankAccount());
        params.add("cardnumber", param.getBankNo());
        params.add("province", "台灣省");
        params.add("city", "台北市");
        params.add("pay_userid", String.valueOf(param.getUid()));
        String signStr = SftSignUtil.formatSignData(params.toSingleValueMap());
        params.add("pay_notifyurl", merchant.getBehalfNotify());
        String sign;
        try {
            sign = SftSignUtil.sign(signType, signStr, key, "TR");
        } catch (Exception e) {
            log.info("sign gen fail : {}", e);
            return Result.failed();
        }
        params.add("pay_md5sign", sign.toUpperCase());
        HttpEntity<MultiValueMap> entity = generateHttpEntity(params, key);

        log.info("=====createBehalfPayOrder url {} before {}", url, entity);
        JSONObject jsonResult = restTemplate.postForObject(url, entity, JSONObject.class);
        log.info("=====createBehalfPayOrder after {}", jsonResult);

        // state狀態碼
        // ew => 新订单
        // processing => 处理中
        // reject => 拒绝
        // completed => 成功
        // failed => 失败
        // refund => 冲回
        String resultStatus = jsonResult.getString("status");
        if ("error".equals(resultStatus)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    jsonResult.getString("msg")
            );
        }

        String transactionId = jsonResult.getString("transaction_id");

        return Result.success(ThirdPartyBehalfPaymentVO.builder().transactionId(transactionId).build());
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkPaymentStatus(Merchant merchant, String orderId) {
        String url = merchant.getPayUrl() + "/Pay_PayQuery_order.html";
        JSONObject apiParameter = merchant.getApiParameter();
        String key = apiParameter.getString("key");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("pay_memberid", apiParameter.getString("memberId"));
        params.add("pay_orderid", orderId);
        String signStr = SftSignUtil.formatSignData(params.toSingleValueMap());
        String sign;
        try {
            sign = SftSignUtil.sign(signType, signStr, key, "TR");
        } catch (Exception e) {
            log.info("sign gen fail : {}", e);
            return Result.failed();
        }
        params.add("pay_md5sign", sign.toUpperCase());
        HttpEntity<MultiValueMap> entity = generateHttpEntity(params, key);

        log.info("=====checkPaymentStatus url {} before {}", url, entity);
        JSONObject jsonResult = JSONObject.parseObject(restTemplate.postForObject(url, entity, String.class));
        log.info("=====checkPaymentStatus after {}", jsonResult);
        String resultStatus = jsonResult.getString("status");
        if ("error".equals(resultStatus)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    jsonResult.getString("refMsg")
            );
        }
//        refCode
//        0 待处理
//        1	成功,商户未返回'OK'
//        2	成功,商户已返回'OK'
//        7	交易不存在
        int orderStatus;
        switch (jsonResult.getInteger("refCode")) {
            case 0:
                orderStatus = OrderStatus.NOT_PROCESSED.getCode();
                break;
            case 1:
            case 2:
                orderStatus = OrderStatus.SUCCESS.getCode();
                break;
            case 7:
                orderStatus = OrderStatus.QUERY_FAIL.getCode();
                break;
            default:
                orderStatus = OrderStatus.OPERATE_FAIL.getCode();
                break;
        }
        return Result.success(
                CheckThirdPartyPaymentVO.builder()
                        .transactionId(jsonResult.getString("transaction_id"))
                        .orderId(jsonResult.getString("out_trade_id"))
                        .amount(jsonResult.getBigDecimal("amount"))
                        .realAmount(jsonResult.getBigDecimal("actualamount"))
                        .status(orderStatus)
                        .build()
        );
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkWithdrawStatus(Merchant merchant, String orderId) {
        String url = merchant.getPayUrl() + "/Payment_Dfpay_query.html";
        JSONObject apiParameter = merchant.getApiParameter();
        String key = apiParameter.getString("key");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("mchid", apiParameter.getString("memberId"));
        params.add("out_trade_no", orderId);
        String signStr = SftSignUtil.formatSignData(params.toSingleValueMap());
        String sign;
        try {
            sign = SftSignUtil.sign(signType, signStr, key, "TR");
        } catch (Exception e) {
            log.info("sign gen fail : {}", e);
            return Result.failed();
        }
        params.add("pay_md5sign", sign.toUpperCase());
        HttpEntity<MultiValueMap> entity = generateHttpEntity(params, key);

        log.info("=====checkWithdrawStatus url {} before {}", url, entity);
        JSONObject jsonResult = JSONObject.parseObject(restTemplate.postForObject(url, entity, String.class));
        log.info("=====checkWithdrawStatus after {}", jsonResult);
        String resultStatus = jsonResult.getString("status");
        if ("error".equals(resultStatus)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    jsonResult.getString("refMsg")
            );
        }
//        refCode
//        1	成功
//        2	失败
//        3	处理中
//        4	待处理
//        5	审核驳回
//        6	待审核
//        7	交易不存在
//        8	未知的错误
        int orderStatus;
        switch (jsonResult.getInteger("refCode")) {
            case 1:
                orderStatus = OrderStatus.SUCCESS.getCode();
                break;
            case 2:
                orderStatus = OrderStatus.TRANSACTION_FAIL.getCode();
                break;
            case 3:
            case 4:
            case 6:
                orderStatus = OrderStatus.PROCESSING.getCode();
                break;
            default:
                orderStatus = OrderStatus.OPERATE_FAIL.getCode();
                break;
        }
        return Result.success(
                CheckThirdPartyPaymentVO.builder()
                        .transactionId(jsonResult.getString("transaction_id"))
                        .orderId(jsonResult.getString("out_trade_no"))
                        .amount(jsonResult.getBigDecimal("amount"))
                        .realAmount(jsonResult.getBigDecimal("amount"))
                        .status(orderStatus)
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
        String url = merchant.getPayUrl() + "/Pay_PayQuery_balance.html";

        JSONObject apiParameter = merchant.getApiParameter();
        String key = apiParameter.getString("key");
        MultiValueMap params = new LinkedMultiValueMap();
        params.add("pay_memberid", apiParameter.getString("memberId"));
        String signStr = SftSignUtil.formatSignData(params.toSingleValueMap());
        String sign;
        try {
            sign = SftSignUtil.sign(signType, signStr, key, "TR");
        } catch (Exception e) {
            log.info("sign gen fail : {}", e);
            return BigDecimal.ZERO;
        }
        params.add("pay_md5sign", sign.toUpperCase());
        HttpEntity<MultiValueMap> request = generateHttpEntity(params, key);

        log.info("=====getCompanyBalance url {} before {}", url, request);
        JSONObject jsonResult = restTemplate.postForEntity(url, request, JSONObject.class).getBody();
        log.info("=====getCompanyBalance after {}", jsonResult);
        String resultStatus = jsonResult.getString("status");
        if ("error".equals(resultStatus)) {
            return BigDecimal.ZERO;
        }
        return jsonResult.getBigDecimal("data");
    }

    /**
     * 生成請求參數
     *
     * @param requestMap 請求參數
     * @param apiKey     api令牌
     * @return
     */
    private HttpEntity<MultiValueMap> generateHttpEntity(MultiValueMap requestMap, String apiKey) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add("Authorization", apiKey);
        return new HttpEntity<>(requestMap, httpHeaders);
    }

}

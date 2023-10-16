package com.c88.payment.service.thirdparty;

import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.payment.pojo.dto.BaseBehalfPayDTO;
import com.c88.payment.pojo.dto.BasePayDTO;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.vo.CheckThirdPartyPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyBehalfPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyPaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.c88.payment.constants.thirdparty.WPayConstants.MERCHANT_CODE;
import static com.c88.payment.constants.thirdparty.WPayConstants.ORDER_STATE_MAP;
import static com.c88.payment.constants.thirdparty.WPayConstants.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class WPayService implements IThirdPartPayService {

    private final RestTemplate restTemplate;

    @Override
    public String getMerchantCode() {
        return MERCHANT_CODE;
    }

    @Override
    public Result<ThirdPartyPaymentVO> createPayOrder(Merchant merchant, BasePayDTO payDTO) {
        String url = merchant.getPayUrl() + "/transfer";
        Map<String, Object> req = generatePayReq(payDTO, merchant);

        log.info("=====createPayOrder url {} before {}", url, req);
        String response = restTemplate.postForObject(url, req, String.class);
        log.info("=====createPayOrder  after {}", response);

        JSONObject jsonResult = JSON.parseObject(response);
        Integer status = jsonResult.getInteger("status");
        if (!SUCCESS.equals(status)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR, "wpay pay error code: " + status);
        }

        ThirdPartyPaymentVO partyPaymentVO = new ThirdPartyPaymentVO();
        partyPaymentVO.setOrderId(jsonResult.getString("order_id"));
        partyPaymentVO.setPayUrl(jsonResult.getString("redirect_url"));
        partyPaymentVO.setAmount(new BigDecimal(jsonResult.getString("amount")));
        return Result.success(partyPaymentVO);
    }

    @Override
    public Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder(Merchant merchant, BaseBehalfPayDTO param) {
        String url = merchant.getPayUrl() + "/daifu";
        Map<String, Object> req = generateBehalfPayReq(param, merchant);
        log.info("=====createBehalfPayOrder url {} before {}", url, req);
        String response = restTemplate.postForObject(url, req, String.class);
        log.info("=====createBehalfPayOrder  after {}", response);

        JSONObject jsonResult = JSON.parseObject(response);
        Integer status = jsonResult.getInteger("status");
        if (!SUCCESS.equals(status)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR, "wpay pay error code: " + status);
        }

        return Result.success(ThirdPartyBehalfPaymentVO.builder().transactionId(param.getOrderId()).build());
    }

    private Map<String, Object> generatePayReq(BasePayDTO payDTO, Merchant merchant) {
        JSONObject apiParameter = merchant.getApiParameter();
        String thirdPartyMerchantId = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        //利用treeMap做排序
        Map<String, Object> req = new TreeMap<>();
        req.put("amount", new BigDecimal(payDTO.getAmount().stripTrailingZeros().toPlainString()));
        req.put("payment_type", payDTO.getChannelId());
        req.put("bank_code", payDTO.getRechargeBankCode());
        req.put("merchant", thirdPartyMerchantId);
        req.put("callback_url", merchant.getNotify());
        req.put("order_id", payDTO.getOrderId());
        req.put("return_url", merchant.getNotify());

        req.put("sign", this.encrypt(req, key));

        return req;
    }

    private Map<String, Object> generateBehalfPayReq(BaseBehalfPayDTO behalfPayDTO, Merchant merchant) {
        JSONObject apiParameter = merchant.getApiParameter();
        String thirdPartyMerchantId = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        //利用treeMap做排序
        Map<String, Object> req = new TreeMap<>();
        req.put("merchant", thirdPartyMerchantId);
        req.put("order_id", behalfPayDTO.getOrderId());
        req.put("callback_url", merchant.getBehalfNotify());
        req.put("total_amount", new BigDecimal(behalfPayDTO.getAmount().stripTrailingZeros().toPlainString()));
        req.put("bank", behalfPayDTO.getBankCode());
        req.put("bank_card_name", behalfPayDTO.getBankAccount());
        req.put("bank_card_account", behalfPayDTO.getBankNo());
        req.put("bank_card_remark", "no");
        req.put("sign", this.encrypt(req, key));
        return req;
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkPaymentStatus(Merchant merchant, String orderId) {
        JSONObject apiParameter = merchant.getApiParameter();
        String thirdPartyMerchantId = apiParameter.getString("code");
        String key = apiParameter.getString("key");
        String apiUrl = apiParameter.getString("apiUrl");

        //利用treeMap做排序
        Map<String, Object> req = new TreeMap<>();
        req.put("merchant", thirdPartyMerchantId);
        req.put("order_id", orderId);
        req.put("sign", this.encrypt(req, key));

        String url = apiUrl + "/query";

        log.info("=====checkPaymentStatus url {} before {}", url, req);
        JSONObject jsonResult = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====checkPaymentStatus after {}", jsonResult);

        String status = jsonResult.getString("status");
        if ("0".equals(status)) {
            return Result.failed(String.format("=====checkPaymentStatus WPay order id : %s Message : %s", orderId, jsonResult.getString("message")));
        }

        CheckThirdPartyPaymentVO checkThirdPartyPaymentVO = new CheckThirdPartyPaymentVO();
        checkThirdPartyPaymentVO.setTransactionId(orderId);
        checkThirdPartyPaymentVO.setOrderId(orderId);
        checkThirdPartyPaymentVO.setStatus(ORDER_STATE_MAP.getOrDefault(status, 4));
        checkThirdPartyPaymentVO.setAmount(jsonResult.getBigDecimal("amount"));
        checkThirdPartyPaymentVO.setRealAmount(jsonResult.getBigDecimal("amount"));
        return Result.success(checkThirdPartyPaymentVO);
    }

    @Override
    public Result<CheckThirdPartyPaymentVO> checkWithdrawStatus(Merchant merchant, String orderId) {
        JSONObject apiParameter = merchant.getApiParameter();
        String thirdPartyMerchantId = apiParameter.getString("code");
        String key = apiParameter.getString("key");
        String apiUrl = apiParameter.getString("apiUrl");

        //利用treeMap做排序
        Map<String, Object> req = new TreeMap<>();
        req.put("merchant", thirdPartyMerchantId);
        req.put("order_id", orderId);
        req.put("sign", this.encrypt(req, key));

        String url = apiUrl + "/query";

        log.info("=====checkWithdrawStatus url {} before {}", url, req);
        JSONObject jsonResult = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====checkWithdrawStatus after {}", jsonResult);

        String status = jsonResult.getString("status");
        if ("0".equals(status)) {
            return Result.failed(String.format("=====checkWithdrawStatus WPay order id : %s Message : %s", orderId, jsonResult.getString("message")));
        }

        CheckThirdPartyPaymentVO checkThirdPartyPaymentVO = new CheckThirdPartyPaymentVO();
        checkThirdPartyPaymentVO.setTransactionId(orderId);
        checkThirdPartyPaymentVO.setOrderId(orderId);
        checkThirdPartyPaymentVO.setStatus(ORDER_STATE_MAP.getOrDefault(status, 4));
        checkThirdPartyPaymentVO.setAmount(jsonResult.getBigDecimal("amount"));
        checkThirdPartyPaymentVO.setRealAmount(jsonResult.getBigDecimal("amount"));
        return Result.success(checkThirdPartyPaymentVO);
    }

    @Override
    public Result<Boolean> setOrderExpired(String order, LocalDateTime localDateTime) {
        return null;
    }

    @Override
    public String encrypt(Map<String, Object> param, String key) {
        String oriStr = this.getUrlStyleRemoveNull(param) + "&key=" + key;
        log.info("*** sign oriStr {}", oriStr);
        String sign = MD5.create().digestHex(oriStr).toLowerCase();
        log.info("*** oriStr after sign {}", sign);
        return sign;
    }

    @Override
    public Boolean checkChannel(Merchant merchant, BasePayDTO payDTO) {
        payDTO.setAmount(BigDecimal.valueOf(5));
        String response = restTemplate.postForObject(merchant.getPayUrl() + "/transfer", generatePayReq(payDTO, merchant), String.class);
        JSONObject jsonResult = JSON.parseObject(response);
        Integer status = jsonResult.getInteger("status");
        log.info(response);
        return Objects.equals(SUCCESS, status);
    }

    @Override
    public BigDecimal getCompanyBalance(Merchant merchant) {
        String url = merchant.getPayUrl() + "/me";
        Map<String, Object> req = generateCompanyBalanceReq(merchant);

        log.info("=====getCompanyBalance url {} before {}", url, req);
        JSONObject jsonResult = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====getCompanyBalance after {}", jsonResult);

        // 取得餘額時不會返回狀態
        BigDecimal balance = jsonResult.getBigDecimal("balance");
        if (Objects.isNull(balance)) {
            log.error("WPay getCompanyBalance fail : {}", jsonResult.getString("message"));
            return BigDecimal.ZERO;
        }

        return balance;
    }

    private Map<String, Object> generateCompanyBalanceReq(Merchant merchant) {
        JSONObject apiParameter = merchant.getApiParameter();
        String thirdPartyMerchantId = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        //利用treeMap做排序
        Map<String, Object> req = new TreeMap<>();
        req.put("merchant", thirdPartyMerchantId);
        req.put("sign", this.encrypt(req, key));

        return req;
    }

}
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrincePayService implements IThirdPartPayService {

    private final RestTemplate restTemplate;

    private static final String MERCHANT_CODE = "prince";

    private static final String PAY_CHANNEL = "712";

    public static final int SUCCESS = 10000;

    @Value("${spring.profiles.active}")
    public String active;

    @Override
    public String getMerchantCode() {
        return MERCHANT_CODE;
    }

    @Override
    public Result<ThirdPartyPaymentVO> createPayOrder(Merchant merchant, BasePayDTO payDTO) {
        String url = merchant.getPayUrl() + "/pay";
        Map<String, Object> req = generatePayReq(payDTO, merchant);

        log.info("=====createPayOrder url {} after {}", url, req);
        String response = restTemplate.postForObject(url, req, String.class);
        log.info("=====createPayOrder before {}", response);

        JSONObject jsonResult = JSON.parseObject(response);
        Integer status = jsonResult.getInteger("status");
        if (SUCCESS != status) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR, "prince pay error code: " + status);
        }

        JSONObject jsonRes = jsonResult.getJSONObject("result");
        ThirdPartyPaymentVO partyPaymentVO = new ThirdPartyPaymentVO();
        partyPaymentVO.setOrderId(jsonRes.getString("transactionid"));
        partyPaymentVO.setPayUrl(jsonRes.getString("payurl"));
        partyPaymentVO.setAmount(new BigDecimal(jsonRes.getString("points")));
        return Result.success(partyPaymentVO);
    }

    @Override
    public Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder(Merchant merchant, BaseBehalfPayDTO param) {
        String url = merchant.getPayUrl() + "/applyfor";
        Map<String, Object> req = generateBehalfPayReq(param, merchant);
        log.info("=====createBehalfPayOrder url {} after {}", url, req);
        String response = restTemplate.postForObject(url, req, String.class);
        log.info("=====createBehalfPayOrder before {}", response);

        JSONObject jsonResult = JSON.parseObject(response);
        Integer status = jsonResult.getInteger("status");
        if (SUCCESS != status) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR, "prince pay error code: " + status);
        }

        JSONObject result = jsonResult.getJSONObject("result");
        String transactionId = result.getString("transactionid");

        return Result.success(ThirdPartyBehalfPaymentVO.builder().transactionId(transactionId).build());
    }

    private Map<String, Object> generatePayReq(BasePayDTO payDTO, Merchant merchant) {
        JSONObject apiParameter = merchant.getApiParameter();
        String thirdPartyMerchantId = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        //利用treeMap做排序
        Map<String, Object> req = new TreeMap<>();
        req.put("uid", thirdPartyMerchantId);
        req.put("orderid", payDTO.getOrderId());
        req.put("channel", payDTO.getChannelId());
        req.put("timestamp", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        req.put("notify_url", merchant.getNotify());
        req.put("return_url", merchant.getNotify());
        req.put("amount", new BigDecimal(payDTO.getAmount().stripTrailingZeros().toPlainString()));
        req.put("userip", payDTO.getUserIp());
        req.put("custom", "");
        if (StringUtils.isNotBlank(payDTO.getRechargeBankCode())) {
            req.put("bank_id", payDTO.getRechargeBankCode());
        }
        req.put("sign", this.encrypt(req, key));

        return req;
    }

    private Map<String, Object> generateBehalfPayReq(BaseBehalfPayDTO behalfPayDTO, Merchant merchant) {
        JSONObject apiParameter = merchant.getApiParameter();
        String thirdPartyMerchantId = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        //利用treeMap做排序
        Map<String, Object> req = new TreeMap<>();
        req.put("uid", thirdPartyMerchantId);
        req.put("orderid", behalfPayDTO.getOrderId());
        req.put("channel", PAY_CHANNEL);
        req.put("timestamp", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        req.put("notify_url", merchant.getBehalfNotify());
        req.put("amount", new BigDecimal(behalfPayDTO.getAmount().stripTrailingZeros().toPlainString()));
        req.put("userip", behalfPayDTO.getUserIp());
        req.put("custom", "");
        req.put("bank_account", behalfPayDTO.getBankAccount());
        req.put("bank_no", behalfPayDTO.getBankNo());
        req.put("bank_id", behalfPayDTO.getBankCode());
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
        req.put("uid", thirdPartyMerchantId);
        req.put("orderid", orderId);
        req.put("timestamp", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        req.put("sign", this.encrypt(req, key));

        String url = apiUrl + "/orderquery";

        log.info("=====checkPaymentStatus url {} before {}", url, req);
        String response = restTemplate.postForObject(url, req, String.class);
        log.info("=====checkPaymentStatus after {}", response);

        JSONObject jsonResult = JSON.parseObject(response);
        Integer status = jsonResult.getInteger("status");
        if (SUCCESS != status) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }
        JSONObject jsonRes = jsonResult.getJSONObject("result");
        JSONObject dataRes = jsonRes.getJSONObject("data");
        JSONObject jsonOrder = dataRes.getJSONObject("0");//只取第一筆

        CheckThirdPartyPaymentVO checkThirdPartyPaymentVO = new CheckThirdPartyPaymentVO();
        checkThirdPartyPaymentVO.setTransactionId(jsonOrder.getString("transactionid"));
        checkThirdPartyPaymentVO.setOrderId(jsonOrder.getString("orderid"));
        checkThirdPartyPaymentVO.setStatus(jsonOrder.getInteger("status"));
        checkThirdPartyPaymentVO.setAmount(jsonOrder.getBigDecimal("amount"));
        checkThirdPartyPaymentVO.setRealAmount(jsonOrder.getBigDecimal("real_amount"));
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
        req.put("uid", thirdPartyMerchantId);
        req.put("orderid", orderId);
        req.put("timestamp", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        req.put("sign", this.encrypt(req, key));

        String url = apiUrl + "/orderquery";

        log.info("=====checkWithdrawStatus url {} before {}", url, req);
        String response = restTemplate.postForObject(url, req, String.class);
        log.info("=====checkWithdrawStatus after {}", response);

        JSONObject jsonResult = JSON.parseObject(response);
        Integer status = jsonResult.getInteger("status");
        if (SUCCESS != status) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR);
        }
        JSONObject jsonRes = jsonResult.getJSONObject("result");
        JSONObject dataRes = jsonRes.getJSONObject("data");
        JSONObject jsonOrder = dataRes.getJSONObject("0");//只取第一筆

        CheckThirdPartyPaymentVO checkThirdPartyPaymentVO = new CheckThirdPartyPaymentVO();
        checkThirdPartyPaymentVO.setTransactionId(jsonOrder.getString("transactionid"));
        checkThirdPartyPaymentVO.setOrderId(jsonOrder.getString("orderid"));
        checkThirdPartyPaymentVO.setStatus(jsonOrder.getInteger("status"));
        checkThirdPartyPaymentVO.setAmount(jsonOrder.getBigDecimal("amount"));
        checkThirdPartyPaymentVO.setRealAmount(jsonOrder.getBigDecimal("real_amount"));
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
        String sign = MD5.create().digestHex(oriStr).toUpperCase();
        log.info("*** oriStr after sign {}", sign);
        return sign;
    }

    @Override
    public Boolean checkChannel(Merchant merchant, BasePayDTO payDTO) {
        payDTO.setAmount(BigDecimal.valueOf(50000));
        String response = restTemplate.postForObject(merchant.getPayUrl() + "/pay", generatePayReq(payDTO, merchant), String.class);
        JSONObject jsonResult = JSON.parseObject(response);
        Integer status = jsonResult.getInteger("status");
        log.info(response);
        return Objects.equals(SUCCESS, status);
    }

    @Override
    public BigDecimal getCompanyBalance(Merchant merchant) {
        // 判斷非正式環境取得固定餘額
        if (!List.of("k8s_pre", "k8s_prod").contains(active)) {
            log.info("active {}", active);
            return BigDecimal.valueOf(1000000);
        }

        String url = merchant.getPayUrl() + "/getpoints";
        Map<String, Object> req = generateCompanyBalanceReq(merchant);

        log.info("=====getCompanyBalance url {} before {}", url, req);
        String response = restTemplate.postForObject(url, req, String.class);
        log.info("=====getCompanyBalance after {}", response);

        JSONObject jsonResult = JSON.parseObject(response);
        Integer status = jsonResult.getInteger("status");
        if (SUCCESS != status) {
            return BigDecimal.ZERO;
        }
        JSONObject result = jsonResult.getJSONObject("result");
        return result.getBigDecimal("points");
    }

    private Map<String, Object> generateCompanyBalanceReq(Merchant merchant) {
        JSONObject apiParameter = merchant.getApiParameter();
        String thirdPartyMerchantId = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        //利用treeMap做排序
        Map<String, Object> req = new TreeMap<>();
        req.put("uid", thirdPartyMerchantId);
        req.put("timestamp", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        req.put("sign", this.encrypt(req, key));

        return req;
    }

}

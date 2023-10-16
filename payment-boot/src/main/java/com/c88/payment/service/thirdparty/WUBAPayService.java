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

import static com.c88.payment.constants.thirdparty.WuBaPayConstant.MERCHANT_CODE;
import static com.c88.payment.constants.thirdparty.WuBaPayConstant.ORDER_STATE_MAP;
import static com.c88.payment.constants.thirdparty.WuBaPayConstant.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class WUBAPayService implements IThirdPartPayService {

    private final RestTemplate restTemplate;

    @Override
    public String getMerchantCode() {
        return MERCHANT_CODE;
    }

    @Override
    public Result<ThirdPartyPaymentVO> createPayOrder(Merchant merchant, BasePayDTO payDTO) {
        String url = merchant.getPayUrl() + "/api/transfer";
        Map<String, Object> req = generatePayReq(payDTO, merchant);

        log.info("=====createPayOrder url {} before {}", url, req);
        JSONObject jsonResult = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====createPayOrder  after {}", jsonResult);

        Integer status = jsonResult.getInteger("status");
        if (!Objects.equals(SUCCESS, status)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR, String.format("58Pay createPayOrder error code: %-5s message : %s", status, jsonResult.getString("message")));
        }

        ThirdPartyPaymentVO partyPaymentVO = new ThirdPartyPaymentVO();
        partyPaymentVO.setOrderId(jsonResult.getString("order_id"));
        partyPaymentVO.setPayUrl(jsonResult.getString("redirect_url"));
        partyPaymentVO.setAmount(jsonResult.getBigDecimal("amount"));
        return Result.success(partyPaymentVO);
    }

    @Override
    public Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder(Merchant merchant, BaseBehalfPayDTO param) {
        String url = merchant.getPayUrl() + "/api/daifu";
        Map<String, Object> req = generateBehalfPayReq(param, merchant);
        log.info("=====createBehalfPayOrder url {} before {}", url, req);
        JSONObject jsonResult = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====createBehalfPayOrder  after {}", jsonResult);

        Integer status = jsonResult.getInteger("status");
        if (!Objects.equals(SUCCESS, status)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR, String.format("58Pay createBehalfPayOrder error code: %-5s message : %s", status, jsonResult.getString("message")));
        }

        return Result.success(ThirdPartyBehalfPaymentVO.builder().transactionId(param.getOrderId()).build());
    }

    private Map<String, Object> generatePayReq(BasePayDTO payDTO, Merchant merchant) {
        JSONObject apiParameter = merchant.getApiParameter();
        String thirdPartyMerchantId = apiParameter.getString("code");
        String key = apiParameter.getString("key");

        //利用treeMap做排序shotcut
        Map<String, Object> req = new TreeMap<>();
        req.put("merchant", thirdPartyMerchantId);
        req.put("payment_type", payDTO.getChannelId());
        req.put("amount", new BigDecimal(payDTO.getAmount().stripTrailingZeros().toPlainString()));
        req.put("order_id", payDTO.getOrderId());
        req.put("bank_code", payDTO.getRechargeBankCode());
        req.put("callback_url", merchant.getNotify());
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

        String url = apiUrl + "/api/query";

        log.info("=====checkPaymentStatus url {} before {}", url, req);
        JSONObject jsonResult = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====checkPaymentStatus after {}", jsonResult);
        CheckThirdPartyPaymentVO checkThirdPartyPaymentVO;
        String status = jsonResult.getString("status");

        if ("0".equals(status)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    String.format("Message : %s",
                            jsonResult.getString("message")
                    )
            );
        }

        checkThirdPartyPaymentVO = new CheckThirdPartyPaymentVO();
        checkThirdPartyPaymentVO.setTransactionId(orderId);
        checkThirdPartyPaymentVO.setOrderId(orderId);
        checkThirdPartyPaymentVO.setStatus(ORDER_STATE_MAP.getOrDefault(status, 3));
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

        String url = apiUrl + "/api/query";

        log.info("=====checkWithdrawStatus url {} before {}", url, req);
        JSONObject jsonResult = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====checkWithdrawStatus after {}", jsonResult);
        CheckThirdPartyPaymentVO checkThirdPartyPaymentVO;
        String status = jsonResult.getString("status");

        if ("0".equals(status)) {
            return Result.failed(ResultCode.INTERNAL_SERVICE_CALLEE_ERROR,
                    String.format("Message : %s",
                            jsonResult.getString("message")
                    )
            );
        }

        checkThirdPartyPaymentVO = new CheckThirdPartyPaymentVO();
        checkThirdPartyPaymentVO.setTransactionId(orderId);
        checkThirdPartyPaymentVO.setOrderId(orderId);
        checkThirdPartyPaymentVO.setStatus(ORDER_STATE_MAP.getOrDefault(status, 3));
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
        payDTO.setAmount(BigDecimal.ZERO);
        String response = restTemplate.postForObject(merchant.getPayUrl() + "/api/transfer", generatePayReq(payDTO, merchant), String.class);
        JSONObject jsonResult = JSON.parseObject(response);
        Integer status = jsonResult.getInteger("status");
        log.info(response);
        return Objects.equals(SUCCESS, status);
    }

    @Override
    public BigDecimal getCompanyBalance(Merchant merchant) {
        String url = merchant.getPayUrl() + "/api/me";
        Map<String, Object> req = generateCompanyBalanceReq(merchant);

        log.info("=====getCompanyBalance url {} before {}", url, req);
        JSONObject jsonResult = restTemplate.postForObject(url, req, JSONObject.class);
        log.info("=====getCompanyBalance after {}", jsonResult);

        return jsonResult.getBigDecimal("balance");
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

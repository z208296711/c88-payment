package com.c88.payment.service.thirdparty;

import cn.hutool.core.collection.CollectionUtil;
import com.c88.common.core.result.Result;
import com.c88.payment.pojo.dto.BaseBehalfPayDTO;
import com.c88.payment.pojo.dto.BasePayDTO;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.vo.CheckThirdPartyPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyBehalfPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyPaymentVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface IThirdPartPayService {

    String URL_PARAM_CONNECT_FLAG = "&";

    /**
     * 建立訂單
     */
    Result<ThirdPartyPaymentVO> createPayOrder(Merchant merchant, BasePayDTO param);

    /**
     * 建立代付訂單
     *
     * @return
     */
    Result<ThirdPartyBehalfPaymentVO> createBehalfPayOrder(Merchant merchant, BaseBehalfPayDTO param);

    /**
     * 確認三方支付狀態
     */
    Result<CheckThirdPartyPaymentVO> checkPaymentStatus(Merchant merchant, String orderId);

    /**
     * 確認三方代付狀態
     */
    Result<CheckThirdPartyPaymentVO> checkWithdrawStatus(Merchant merchant, String orderId);

    /**
     * 設定訂單過期時間
     */
    Result<Boolean> setOrderExpired(String order, LocalDateTime localDateTime);

    /**
     * 商戶代碼
     */
    String getMerchantCode();

    /**
     * 參數加密
     */
    String encrypt(Map<String, Object> param, String key);

    /**
     * 測試通道
     */
    Boolean checkChannel(Merchant merchant, BasePayDTO payDTO);

    /**
     * 查詢目前餘額
     */
    BigDecimal getCompanyBalance(Merchant merchant);

    default String getUrlStyleRemoveNull(Map map) {
        if (CollectionUtil.isEmpty(map)) {
            return ("");
        }
        StringBuilder url = new StringBuilder();
        for (Object obj : map.keySet()) {
            String key = String.valueOf(obj);
            if (map.containsKey(key)) {
                Object val = map.get(key);
                if (null != val) {
                    url.append(key).append("=").append(val.toString())
                            .append(URL_PARAM_CONNECT_FLAG);
                }
            }
        }
        String strURL;
        strURL = url.toString();
        if (URL_PARAM_CONNECT_FLAG.equals("" + strURL.charAt(strURL.length() - 1))) {
            strURL = strURL.substring(0, strURL.length() - 1);
        }
        return (strURL);
    }

}

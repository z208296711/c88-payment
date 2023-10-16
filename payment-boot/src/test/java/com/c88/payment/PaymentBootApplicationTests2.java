package com.c88.payment;

import com.c88.common.core.result.Result;
import com.c88.payment.pojo.dto.BasePayDTO;
import com.c88.payment.pojo.vo.CheckThirdPartyPaymentVO;
import com.c88.payment.pojo.vo.ThirdPartyPaymentVO;
import com.c88.payment.service.thirdparty.PrincePayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest(properties = "spring.profiles.active:local")
class PaymentBootApplicationTests2 {

    @Autowired
    private PrincePayService princePayService;

    @Test
    void testPay() {
        BasePayDTO dto = new BasePayDTO();
        dto.setOrderId("TEST0000015");
        dto.setChannelId("909");
        dto.setAmount(new BigDecimal(50000));
        // Result<ThirdPartyPaymentVO> result = princePayService.createPayOrder(dto);
        System.out.println();
    }

    @Test
    void testOrderquery() throws Exception {
        // Result<CheckThirdPartyPaymentVO> checkThirdPartyPaymentVOResult = princePayService.checkPaymentStatus("TEST0000015");
        // System.out.println(checkThirdPartyPaymentVOResult);
    }

}

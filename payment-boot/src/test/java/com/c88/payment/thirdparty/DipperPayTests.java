package com.c88.payment.thirdparty;

import com.c88.common.core.result.Result;
import com.c88.payment.pojo.dto.BasePayDTO;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.vo.ThirdPartyPaymentVO;
import com.c88.payment.service.IMemberRechargeService;
import com.c88.payment.service.IMerchantService;
import com.c88.payment.service.thirdparty.DipperPayService;
import com.c88.payment.service.thirdparty.PrincePayService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest(properties = "spring.profiles.active:local")
class DipperPayTests {

    @Autowired
    private DipperPayService dipperPayService;

    @Autowired
    private IMerchantService iMerchantService;

    @Autowired
    private IMemberRechargeService iMemberRechargeService;

    @Test
    void testPay() {

        Merchant merchant = iMerchantService.getById(26);
        BasePayDTO dto = new BasePayDTO();
        dto.setOrderId("TEST0000001");
        dto.setChannelId("909");
        dto.setAmount(new BigDecimal(50000));
        Result<ThirdPartyPaymentVO> result = dipperPayService.createPayOrder(merchant, dto);

    }

    @Test
    void testOrderquery() throws Exception {
        iMerchantService.findMerchantState(19);
        // Result<CheckThirdPartyPaymentVO> checkThirdPartyPaymentVOResult = princePayService.checkPaymentStatus("TEST0000015");
        // System.out.println(checkThirdPartyPaymentVOResult);
    }

}

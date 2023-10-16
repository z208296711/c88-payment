package com.c88.payment.client;

import com.c88.common.core.result.Result;
import com.c88.member.vo.OptionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "c88-payment", path = "/payment/api/v1/recharge/type")
public interface MemberRechargeTypeClient {

    @GetMapping("/option")
    Result<List<OptionVO<Integer>>> findRechargeTypeOption();

}

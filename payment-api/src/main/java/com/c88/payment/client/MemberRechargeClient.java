package com.c88.payment.client;

import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.payment.form.SearchMemberDepositForm;
import com.c88.payment.vo.MemberDepositDTO;
import com.c88.payment.vo.MemberRechargeDTO;
import com.c88.payment.vo.MemberTotalRechargeVO;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "c88-payment", path = "/payment/api/v1/recharge")
public interface MemberRechargeClient {

    @GetMapping("/total/{uid}")
    Result<BigDecimal> memberTotalRecharge(@PathVariable Long uid);

    @GetMapping("/from/{uid}")
    Result<BigDecimal> memberTotalRechargeFrom(@PathVariable Long uid, @Parameter LocalDateTime fromTime);

    @GetMapping("/from/to/{uid}")
    Result<BigDecimal> memberTotalRechargeFromTo(@PathVariable Long uid,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime);


    @GetMapping("/member/total")
    Result<List<MemberTotalRechargeVO>> findMemberTotalRecharge(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime);


    @GetMapping("/member/{uid}/single/recharge")
    Result<List<MemberRechargeDTO>> findMemberSingleRecharge(@PathVariable Long uid,
                                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime,
                                                             @RequestParam BigDecimal amount);

    @GetMapping("/member/{username}/recharge")
    PageResult<MemberDepositDTO> findMemberRecharges(@PathVariable String username,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beginTime,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                     @RequestParam(required = false, defaultValue = "1") int pageNum,
                                                     @RequestParam(required = false, defaultValue = "10") int pageSize);
}

package com.c88.payment;

import com.c88.admin.api.TagFeignClient;
import com.c88.affiliate.api.feign.AffiliateFeignClient;
import com.c88.affiliate.api.feign.AffiliateMemberClient;
import com.c88.feign.RiskFeignClient;
import com.c88.game.adapter.api.GameFeignClient;
import com.c88.member.api.H5MemberFeignClient;
import com.c88.member.api.MemberFeignClient;
import com.c88.payment.client.MemberRechargeClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(
        basePackages = {"com.c88.payment.mapper"}
)
@EnableFeignClients(
        basePackageClasses = {
                H5MemberFeignClient.class,
                MemberFeignClient.class,
                TagFeignClient.class,
                GameFeignClient.class,
                MemberRechargeClient.class,
                RiskFeignClient.class,
                AffiliateFeignClient.class,
                AffiliateMemberClient.class
        }
)
@SpringBootApplication
public class PaymentBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentBootApplication.class, args);
    }

}

package com.c88.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "用戶首充訊息")
public class MemberFirstRechargeDTO implements Serializable {

    @Schema(title = "訂單號")
    private String orderId;

}
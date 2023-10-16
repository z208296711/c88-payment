package com.c88.payment.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.c88.payment.enums.MemberRechargeTypeEnum;
import com.c88.payment.enums.PayStatusEnum;
import com.c88.payment.enums.RechargeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportOnlineMemberRechargeVO {

    @ExcelProperty("充值單號")
    private String tradeNo;

    /**
     * {@link MemberRechargeTypeEnum}
     */
    @ExcelProperty("充值類型")
    private String type;

    @ExcelProperty("會員帳號")
    private String username;

    @ExcelProperty("會員等級")
    private String level;

    @ExcelProperty("充值金額")
    private String amount;

    @ExcelProperty("參加優惠")
    private String deposit;

    @ExcelProperty("手續費")
    private String fee;

    @ExcelProperty("實際到帳金額")
    private String realAmount;

    /**
     * {@link RechargeTypeEnum}
     */
    @ExcelProperty("支付方式")
    private String rechargeTypeName;

    @ExcelProperty("充值銀行")
    private String bank;

    @ExcelProperty("商戶名稱")
    private String merchantName;

    /**
     * {@link PayStatusEnum}
     */
    @ExcelProperty("狀態")
    private String status;

    @ExcelProperty("備註")
    private String remark;

    @ExcelProperty("申請時間")
    private String createTime;

    @ExcelProperty("到帳時間")
    private String transactionTime;

}

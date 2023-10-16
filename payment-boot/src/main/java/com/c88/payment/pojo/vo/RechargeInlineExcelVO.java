package com.c88.payment.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeInlineExcelVO {

    @ExcelProperty("充值類型")
    @Schema(title = "充值單類型", description = "0在線充值, 1手動充值, 2手動補單")
    private String type;

    @ExcelProperty("充值單號")
    private String tradeNo;

    @ExcelProperty("會員帳號")
    private String username;

    @ExcelProperty("真實姓名")
    private String realName;

    @ExcelProperty("會員等級")
    private String level;

    @ExcelProperty("充值金額")
    private BigDecimal amount;

    @ExcelProperty("參加優惠")
    private String activityDeposit = "無";

    @ExcelProperty("自營卡代號")
    private String bankCardCode;

    @ExcelProperty("充值銀行")
    private String bank;

    @ExcelProperty("狀態")
    @Schema(title = "狀態", description = "0處理中, 1成功, 2失敗")
    private String status;

    @ExcelProperty("附言")
    private String notes;

    @ExcelProperty("備註")
    private String remark;

    @ExcelProperty("申請時間")
    private String gmtCreate;

    @ExcelProperty("到帳時間")
    private String successTime;

    @ExcelProperty("操作")
    private String checkUser;
}

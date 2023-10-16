package com.c88.payment.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 資金流動紀錄
 */
@Data
public class AdminBalanceChangeRecordVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 會員Id
     */
    @Schema(title = "會員Id")
    private Long memberId;

    /**
     * 帳號
     */
    @Schema(title = "帳號")
    private String username;

    /**
     * 帳變類型  1:充值 2:提款 3:紅利：包含項目像是紅包、禮金、免費籌碼以及返水等等（不含存送優惠）4:清零：會員餘額低於某指定金額則清空該會員距離提現的總要求流水為0 5:轉帳：三方與平台間的金額移轉動作 6:佣金：代理佣金轉移 7:調整：手動調整累積要求流水 8:存送優惠
     */
    @Schema(title = "帳變類型  1:充值 2:提款 3:紅利：包含項目像是紅包、禮金、免費籌碼以及返水等等（不含存送優惠）4:清零：會員餘額低於某指定金額則清空該會員距離提現的總要求流水為0 5:轉帳：三方與平台間的金額移轉動作 6:佣金：代理佣金轉移 7:調整：手動調整累積要求流水 8:存送優惠")
    private Integer type;

    /**
     * 擴展信息
     */
    @Schema(title = "擴展信息")
    private String note;

    /**
     * 變動金額
     */
    @Schema(title = "變動金額")
    private BigDecimal amount;

    /**
     * 當前可用餘額
     */
    @Schema(title = "當前可用餘額")
    private BigDecimal currentBalance;

    /**
     * 打碼要求倍數
     */
    @Schema(title = "打碼要求倍數")
    private BigDecimal betRate;

    /**
     * 要求流水
     */
    @Schema(title = "要求流水")
    private BigDecimal needBet;

    /**
     * 存送溢出要求流水
     */
    @Schema(title = "存送溢出要求流水")
    private BigDecimal rechargeAwardFullOut;


    /**
     * 有效流水: 距離提現所累積的有效流水，並非帳號至今為止的有效流水
     */
    @Schema(title = "有效流水: 距離提現所累積的有效流水，並非帳號至今為止的有效流水")
    private BigDecimal validBet;

    /**
     * 累計要求流水: 當前累計要求流水+此筆帳變的要求流水
     */
    @Schema(title = "累計要求流水: 當前累計要求流水+此筆帳變的要求流水")
    private BigDecimal accBet;


    @Schema(title = "帳變時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;


}
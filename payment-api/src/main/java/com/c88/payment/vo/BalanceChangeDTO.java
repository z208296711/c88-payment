package com.c88.payment.vo;

import com.c88.payment.enums.BalanceChangeTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 資金流動紀錄
 *
 * @TableName payment_balance_change_record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "資金流動紀錄")
public class BalanceChangeDTO {

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
     *
     * @see BalanceChangeTypeEnum
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
    @Schema(title = "金額-變動前")
    private BigDecimal beforeBalance;

    /**
     * 變動金額
     */
    @Schema(title = "金額-變動後")
    private BigDecimal afterBalance;

    /**
     * 變動金額
     */
    @Schema(title = "變動金額")
    private BigDecimal amount;

    /**
     * 打碼要求倍數
     */
    @Schema(title = "打碼要求倍數")
    private BigDecimal betRate = BigDecimal.ZERO;


    @Schema(title = "帳變時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreate;

    @Schema(title = "存送優惠相關訊息 有存送優惠就必傳 沒有就傳null")
    private RechargeAwardDTO rechargeAwardDTO;

}
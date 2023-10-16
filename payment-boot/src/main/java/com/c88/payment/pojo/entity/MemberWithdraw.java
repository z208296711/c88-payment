package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import com.c88.common.mybatis.handler.IntegerArrayJsonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_member_withdraw")
public class MemberWithdraw extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(title = "id")
    private Integer id;

    @Schema(title = "提款單號")
    private String withdrawNo;

    @Schema(title = "會員/代理 Id")
    private Long uid;

    @Schema(title = "type 0:會員 1:代理")
    private Integer type;

    @Schema(title = "帳號")
    private String username;

    @Schema(title = "真實姓名（開戶名）")
    private String realName;

    @Schema(title = "銀行卡ID")
    private Integer bankId;

    @Schema(title = "銀行卡號")
    private String bankCardNo;

    @Schema(title = "提幣名稱")
    private String cryptoName;

    @Schema(title = "提幣地址")
    private String cryptoAddress;

    @Schema(title = "提款當下餘額")
    private BigDecimal currentBalance;

    @Schema(title = "提款金額")
    private BigDecimal amount;

    @Schema(title = "提款狀態", description = "0_待審核, 2_提交二審, 3_審核通過, 4_審核拒絕")
    private Byte state;

    @Size(max = 200)
    @Schema(title = "一審備註")
    private String firstNote;

    @Size(max = 200)
    @Schema(title = "二審備註")
    private String secondNote;

    @TableField(typeHandler = IntegerArrayJsonTypeHandler.class)
    @Schema(title = "會員標籤id", hidden = true)
    private Integer[] tagIds;

    @Schema(title = "會員標籤")
    @TableField(exist = false)
    private List<String> tags;

    @Schema(title = "會員等級id", hidden = true)
    private Integer vipId;

    @Schema(title = "會員等級")
    @TableField(exist = false)
    private String vip;

    @Schema(title = "申請時間")
    private LocalDateTime applyTime;

    @Schema(title = "一審領取時間")
    private LocalDateTime firstPickTime;

    @Schema(title = "一審時間（處置時間）")
    private LocalDateTime firstTime;

    @Schema(title = "二審領取時間")
    private LocalDateTime secondPickTime;

    @Schema(title = "二審時間（處置時間）")
    private LocalDateTime secondTime;

    @Schema(title = "一審人員（領取人員與處置人員相同）")
    private String firstUser;

    @Schema(title = "二審人員（領取人員與處置人員相同）")
    private String secondUser;

    @Schema(title = "提交IP")// 提交申請時的IP
    private String applyIp;

    @Schema(title = "出款人員（領取人員與處置人員相同）")
    private String remitUser;

    @Schema(title = "出款狀態", description = "0_待領取, 1_人工出款中, 2_人工出款成功, 3_人工取消出款, 4_自動代付成功, 5_代付出款中, 6_自動代付失敗, 7_自動代付失敗（實名失敗）, 8_撤銷出款")
    private Byte remitState;

    @Size(max = 200)
    @Schema(title = "出款備註")
    private String remitNote;

    @Schema(title = "出款領取時間")
    private LocalDateTime remitPickTime;

    @Schema(title = "出款時間（處置時間）")
    private LocalDateTime remitTime;

    @Schema(title = "商戶id")
    private Long merchantId;

    @Schema(title = "自營卡id")
    private Long companyBankCardId;

    @Schema(title = "三方代付回傳的訂單id", hidden = true)
    private String transactionId;

}

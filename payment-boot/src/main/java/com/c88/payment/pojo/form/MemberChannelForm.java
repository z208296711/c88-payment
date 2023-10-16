package com.c88.payment.pojo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(title = "會員通道表格")
public class MemberChannelForm {

    @Schema(title = "通道id", description = "更新時在給")
    private Long id;

    @Schema(title = "通道類型", description = "1自營卡 2商戶(三方)")
    private Integer type;

    @Schema(title = "商戶支付方式")
    private Integer rechargeTypeId;

    @Schema(title = "商戶代碼")
    private String merchantCode;

    @Schema(title = "商戶名稱id")
    private Integer merchantId;

    @Schema(title = "商戶名稱")
    private String merchantName;

    @Schema(title = "自營卡群組id")
    private Long companyBankCardGroupId;

    @Schema(title = "會員支付名稱")
    private String rechargeShowName;

    @Schema(title = "會員通道名稱")
    private String channelName;

    @Schema(title = "指派等級")
    private List<Long> vipIds;

    @Schema(title = "指派標籤")
    private List<Long> memberTags;

    @Schema(title = "單筆充值最大金額")
    private BigDecimal maxRechargeAmount;

    @Schema(title = "單筆充值最小金額")
    private BigDecimal minRechargeAmount;

    @Schema(title = "備註")
    private String remark;

    @Schema(title = "通道狀態", description = "0停用 1啟用")
    private Integer status;
}

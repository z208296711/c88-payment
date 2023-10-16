package com.c88.payment.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.c88.common.mybatis.handler.StringArrayJsonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "出款代付管理")
public class MerchantPayVO {

    @Schema(title = "三方支付ID")
    private Integer id;

    @Schema(title = "三方支付英文代号")
    private String merchantCode;

    @Schema(title = "餘額下限水位")
    private BigDecimal thresholdBalance;

    @Schema(title = "即時餘額")
    private BigDecimal realBalance;

    @Schema(title = "處理中餘額")
    private BigDecimal processBalance;

    @Schema(title = "單筆上限金額")
    private BigDecimal maxAmount;

    @Schema(title = "單筆下限金額")
    private BigDecimal minAmount;

    @Schema(title = "啟用開關", description = " 0:關 1:開")
    private Integer agentWithdrawEnable;

    @Schema(title = "可開放的會員等級ID")
    private List<Integer> vipIds;

    @Schema(title = "限定可用的會員標籤ID")
    private List<Integer> tagIds;

    @Schema(title = "指派等級")
    private List<VipVO> vipVOS;

    @Schema(title = "指派標籤")
    private List<TagVO> tagVOS;

    @Schema(title = "啟用開關", description = "0:關 1:開")
    private Integer enable;

    @Schema(title = "備註")
    private String note;
}

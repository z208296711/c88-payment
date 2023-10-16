package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 公司自營卡
 * @TableName payment_company_bank_card
 */
@TableName(value ="payment_company_bank_card")
@Data
public class CompanyBankCard extends BaseEntity implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收款人名稱
     */
    private String owner;

    /**
     * 代號
     */
    private String code;

    /**
     * 自營卡群組ID
     */
    private Integer groupId;

    /**
     * 銀行ID
     */
    private Integer bankId;

    /**
     * 銀行卡號
     */
    private String bankCardNo;

    /**
     * 單日存款上線總額
     */
    private BigDecimal dailyMaxAmount;

    /**
     * 單筆存款上限
     */
    private BigDecimal maxAmount;

    /**
     * 單筆存款下限
     */
    private BigDecimal minAmount;

    /**
     * 轉帳說明
     */
    private String note;

    /**
     * 是否產生附言 0不產生1產生
     */
    private Integer comments;

    /**
     * 刪除註記 0沒刪除1已刪除
     */
    @TableField(value = "deleted")
    @TableLogic
    private Integer deleted;

    /**
     * 啟用狀態 0停用1啟用
     */
    private Integer enable;

    /**
     * 更新時間
     */
    private LocalDateTime gmtModified;

    /**
     * 創建時間
     */
    private LocalDateTime gmtCreate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}
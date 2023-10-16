package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.c88.common.core.base.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 自營卡群組
 * @TableName payment_company_bank_card_group
 */
@TableName(value ="payment_company_bank_card_group")
@Data
public class CompanyBankCardGroup extends BaseEntity implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 名稱
     */
    private String name;

    /**
     * 註記
     */
    private String note;



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
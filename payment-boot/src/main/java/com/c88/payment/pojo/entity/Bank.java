package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.c88.common.core.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 銀行
 *
 * @TableName payment_bank
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "payment_bank")
public class Bank extends BaseEntity implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 銀行縮寫
     */
    @TableField(value = "code")
    private String code;

    /**
     * 名稱
     */
    @TableField(value = "name")
    private String name;

    /**
     * 名稱(英文)
     */
    @TableField(value = "name_en")
    private String nameEn;

    /**
     * 銀行logo
     */
    @TableField(value = "logo")
    private String logo;

    /**
     * 提款名稱
     */
    @TableField(value = "withdraw_name")
    private String withdrawName;

    /**
     * 充值名稱
     */
    @TableField(value = "recharge_name")
    private String rechargeName;

    /**
     * 註記
     */
    @TableField(value = "note")
    private String note;

    /**
     * 銀行狀態 0:維護中 1:啟用中 2:排程中
     */
    @TableField(value = "state")
    private Integer state;

    /**
     * 每日維護時間 0關閉1啟動
     */
    @TableField(value = "daily_enable")
    private Integer dailyEnable;

    /**
     * 維護開始時間
     */
    @TableField(value = "daily_start_time")
    private LocalTime dailyStartTime;

    /**
     * 維護結束時間
     */
    @TableField(value = "daily_end_time")
    private LocalTime dailyEndTime;

    /**
     * 指定維護時間 0關閉1啟動
     */
    @TableField(value = "assign_enable")
    private Integer assignEnable;

    /**
     * 指定維護開始時間
     */
    @TableField(value = "assign_start_time")
    private LocalDateTime assignStartTime;

    /**
     * 指定維護結束時間
     */
    @TableField(value = "assign_end_time")
    private LocalDateTime assignEndTime;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 是否被刪除 0否1是
     */
    @TableLogic
    @TableField(value = "deleted")
    private Integer deleted;

}
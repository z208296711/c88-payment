package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.c88.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MemberRemark extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(title = "會員id")
    private Long id;
    @Schema(title = "會員ID")
    private Long uid;
    @Schema(title = "備註內容")
    private String content;
    @Schema(title = "備註管理員帳號")
    private String admin;

}

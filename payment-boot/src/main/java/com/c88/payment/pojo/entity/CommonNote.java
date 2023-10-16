package com.c88.payment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.c88.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

@Schema(title = "常用備註")
@Data
public class CommonNote extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;
    @Schema(title = "訊息分類", description = "2_轉二審, 3_通過, 4_拒絕")
    private Byte type;
    @Schema(title = "審級", description = "1_一審, 2_二審")
    private Byte level;
    @Size(max = 32)
    @Schema(title = "選項標題")
    private String title;
    @Size(max = 200)
    @Schema(title = "訊息內容")
    private String content;
    @Schema(title = "最後編輯者")
    private String updater;
    @Schema(title = "會員分類", description = "0會員,1代理")
    private Integer memberType;

}

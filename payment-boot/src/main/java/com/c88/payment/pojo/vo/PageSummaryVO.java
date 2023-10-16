package com.c88.payment.pojo.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.util.Objects;
@Data
public class PageSummaryVO {
    IPage page;
    Object object;
}

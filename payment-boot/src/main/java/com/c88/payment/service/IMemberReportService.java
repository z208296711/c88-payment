package com.c88.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.c88.payment.pojo.form.SearchMemberWinLossForm;
import com.c88.payment.pojo.vo.MemberWinLossVO;

/**
 * @description: TODO
 * @author: marcoyang
 * @date: 2022/12/29
 **/
public interface IMemberReportService {

    IPage<MemberWinLossVO> getMemberWinLoss(SearchMemberWinLossForm form);
}

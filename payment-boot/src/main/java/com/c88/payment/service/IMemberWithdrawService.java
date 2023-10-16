package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.entity.MemberWithdraw;
import com.c88.payment.pojo.form.FindRemitForm;
import com.c88.payment.pojo.form.FindWithdrawForm;
import com.c88.payment.pojo.form.WithdrawForm;
import com.c88.payment.pojo.vo.withdraw.RemitReportVO;
import com.c88.payment.pojo.vo.withdraw.RemitVO;
import com.c88.payment.pojo.vo.withdraw.WithdrawReportVO;
import com.c88.payment.pojo.vo.withdraw.WithdrawVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提款申請管理
 */
@Service
public interface IMemberWithdrawService extends IService<MemberWithdraw> {

    Page<WithdrawVO> queryWithdraw(FindWithdrawForm form);

    List<WithdrawReportVO> listWithdraw(FindWithdrawForm form);

    Page<RemitVO> queryRemit(FindRemitForm form);

    List<RemitReportVO> listRemit(FindRemitForm form);

    WithdrawVO getDetail(Long id);

    boolean approve(WithdrawForm withdrawForm, String ip);

    /**
     * 執行自動代付
     *
     * @param withdraw
     * @param merchant
     * @param ip
     * @return
     */
    boolean executeMerchantPay(MemberWithdraw withdraw, Merchant merchant, String ip);

    /**
     * 從代付資料中判斷符合會員等級、標籤，三方餘額、單筆上下限，支援銀行的三方代付
     *
     * @return 合用的三方代付，若沒有則為 null
     */
    Merchant checkSuitableMerchant(MemberWithdraw withdraw);

    /**
     * 檢查自動代付結果
     *
     * @param id 提款訂單id
     * @return
     */
    boolean checkPayState(Integer id);

    /**
     * 取得會員所有已審核通過提款金額（包含人工付款中或自動代付中）（不包含付款失敗或撤銷）
     *
     * @param uid
     * @return
     */
    BigDecimal memberTotalWithdraw(Long uid);

}

package com.c88.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.c88.payment.pojo.form.AdminSearchAffiliateMemberTransferForm;
import com.c88.payment.pojo.form.AdminSearchAffiliateTransferForm;
import com.c88.payment.pojo.form.AffiliateTransferForm;
import com.c88.payment.pojo.form.SearchAffiliateTransferForm;
import com.c88.payment.pojo.vo.AdminAffiliateMemberTransferRecordVO;
import com.c88.payment.pojo.vo.AdminAffiliateTransferRecordVO;
import com.c88.payment.pojo.vo.AffiliateMemberTransferRecordVO;
import com.c88.payment.pojo.vo.AffiliateTransferRecordVO;

import java.util.concurrent.ExecutionException;

public interface IAffiliateTransferService {

    Boolean memberTransfer(Long affiliateId, String ip, AffiliateTransferForm form) throws ExecutionException, InterruptedException;

    Boolean transfer(Long affiliateId, String ip, AffiliateTransferForm form) throws ExecutionException, InterruptedException;

    IPage<AffiliateTransferRecordVO> findTransferRecordPage(Long affiliateId, SearchAffiliateTransferForm form);

    IPage<AffiliateMemberTransferRecordVO> findMemberTransferRecordPage(Long affiliateId, SearchAffiliateTransferForm form);

    IPage<AdminAffiliateTransferRecordVO> findAdminTransferRecordPage(AdminSearchAffiliateTransferForm form);

    IPage<AdminAffiliateMemberTransferRecordVO> findAdminMemberTransferRecordPage(AdminSearchAffiliateMemberTransferForm form);


}

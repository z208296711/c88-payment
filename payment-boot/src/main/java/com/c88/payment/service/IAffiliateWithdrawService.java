package com.c88.payment.service;

import com.c88.payment.pojo.form.AffiliateWithdrawApplyForm;

public interface IAffiliateWithdrawService {

    Boolean withdrawApply(Long affiliateId,String ip, AffiliateWithdrawApplyForm form);

}

package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.payment.mapper.MerchantPayBankMapper;
import com.c88.payment.pojo.entity.MerchantPayBank;
import com.c88.payment.service.IMerchantPayBankService;
import org.springframework.stereotype.Service;

/**
 * @author user
 * @description 针对表【payment_merchant_pay_bank(第三方廠商代付銀行)】的数据库操作Service实现
 * @createDate 2022-08-24 14:32:12
 */
@Service
public class MerchantPayBankServiceImpl extends ServiceImpl<MerchantPayBankMapper, MerchantPayBank>
        implements IMerchantPayBankService {

}





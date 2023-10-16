package com.c88.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.payment.pojo.entity.WithdrawRisk;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public interface IWithdrawRiskService extends IService<WithdrawRisk> {

    /**
     * 批次寫入，忽略重複key的資料
     *
     * @param entityList
     * @return
     */
    boolean saveBatchIgnore(Collection<WithdrawRisk> entityList);

}

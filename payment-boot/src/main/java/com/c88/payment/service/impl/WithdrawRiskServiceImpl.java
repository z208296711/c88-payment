package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.payment.mapper.WithdrawRiskMapper;
import com.c88.payment.pojo.entity.WithdrawRisk;
import com.c88.payment.service.IWithdrawRiskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
public class WithdrawRiskServiceImpl extends ServiceImpl<WithdrawRiskMapper, WithdrawRisk> implements IWithdrawRiskService {

    /**
     * 批次寫入，忽略重複key的資料
     *
     * @param entityList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveBatchIgnore(Collection<WithdrawRisk> entityList) {
        String sqlStatement = getSqlStatement(SqlMethod.INSERT_ONE);
        sqlStatement += "Ignore";
        String finalSqlStatement = sqlStatement;
        return executeBatch(entityList, DEFAULT_BATCH_SIZE, (sqlSession, entity) -> sqlSession.insert(finalSqlStatement, entity));
    }

}

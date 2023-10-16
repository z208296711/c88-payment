package com.c88.payment.tasks;

import com.c88.common.core.constant.GlobalConstants;
import com.c88.payment.mapstruct.BankConverter;
import com.c88.payment.pojo.entity.Bank;
import com.c88.payment.pojo.form.MaintainBankForm;
import com.c88.payment.service.IBankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class BankMaintainTask {

    private final IBankService iBankService;

    private final BankConverter bankConverter;

    @Scheduled(cron = "0 0 7 * * ?", zone = "UTC")
    public void bankMaintainTask() {
        log.info("==========bankMaintainTask-Start==========");
        List<Bank> bankWillMaintains = iBankService.lambdaQuery()
                .gt(Bank::getAssignStartTime, LocalDateTime.now())
                .or()
                .eq(Bank::getDailyEnable, GlobalConstants.STATUS_YES)
                .gt(Bank::getDailyStartTime, LocalDateTime.now())
                .list();

        if (CollectionUtils.isNotEmpty(bankWillMaintains)) {
            bankWillMaintains.forEach(bank -> {
                MaintainBankForm form = bankConverter.toMaintainBankForm(bank);
                form.setId(bank.getId());
                iBankService.bankMaintainTimeProcess(form);
            });
        }
        log.info("==========bankMaintainTask-End==========");
    }

}

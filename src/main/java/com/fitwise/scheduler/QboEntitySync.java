package com.fitwise.scheduler;

import com.fitwise.service.qbo.QBOService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QboEntitySync {

    @Autowired
    QBOService qboService;


    @Scheduled(fixedDelay = 120000, initialDelay = 60000)
    public void syncAllQboEntity() {
        log.info("All Entity sync started");
        qboService.syncAllEntities();
        log.info("All Entity sync ended");
    }

    /**
     * Method used to check the current status of transactions in Authorize.net
     * <p>
     * Scheduler invoked at 00:05 AM server time - everyday
     */
    @Scheduled(cron = "1 5 0 * * * ")
    public void syncWebhookNotCapturedBillPayments(){
        log.info("Bill Payment sync started");
        qboService.retrieveVendorBillPaidTransactions();
        log.info("Bill Payment sync completed");
    }

}

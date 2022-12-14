package com.fitwise.scheduler;

import com.fitwise.service.itms.ITMSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ITMSEntitySync {

    @Autowired
    private ITMSService itmsService;


    //@Scheduled(cron = "1 */5 * * * *")  // Stopping ITMS sync service due to IAP remove in iOS app.
    /*public void syncAllITMSEntity() {
        log.info("All Entity sync started for ITMS.");
        itmsService.syncAll();
        log.info("All Entity sync ended ITMS.");
    }*/

    /**
     * Send feedback through email
     */
    /*@Scheduled(cron = "1 32 11 * * * ")
    public void sendDailySyncStatus() {
        log.info("All Entity sync started for ITMS.");
        itmsService.sendDailyFeedback();
        itmsService.cleanupIapFolders();
        log.info("All Entity sync ended .");
    }*/
    
    /**
     * Remove Expired Discounts from Appstore (Daily:start of the day)
     */
    /*@Scheduled(cron = "0 0 * * * *")
	public void removeExpiredDiscounts() {
		 log.info("Remove Expired Discounts from Appstore.");
		 itmsService.removeExpiredDiscounts();
		 log.info("End : Remove Expired Discounts from Appstore.");
	}*/
}

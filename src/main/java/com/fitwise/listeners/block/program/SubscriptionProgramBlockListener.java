package com.fitwise.listeners.block.program;

import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.itms.FitwiseITMSUploadEntityService;
import com.fitwise.service.payments.appleiap.IAPServerNotificationService;
import com.fitwise.service.payments.appleiap.InAppPurchaseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Created by Vignesh G on 29/06/20
 */
@Component
public class SubscriptionProgramBlockListener implements ProgramBlockListener {

    @Autowired
    SubscriptionService subscriptionService;
    
    @Autowired
    InAppPurchaseService inAppPurchaseService;
    
    @Autowired
    IAPServerNotificationService iapServerNotificationService;

    @Autowired
    private FitwiseITMSUploadEntityService fitwiseITMSUploadEntityService;
    /**
     * Subscription related events for program block
     * @param programBlockEvent
     */
    @Override
    public void programBlocked(ProgramBlockEvent programBlockEvent) {
        List<ProgramSubscription> programSubscriptions = subscriptionService.getPaidSubscriptionsOfProgram(programBlockEvent.getProgramId());
        for (ProgramSubscription programSubscription : programSubscriptions) {
            if (programSubscription.isAutoRenewal()) {
                if (programSubscription != null && programSubscription.getSubscribedViaPlatform() != null) {
                    if (programSubscription.getSubscribedViaPlatform().getPlatformTypeId() == 2) { 
                    	
                    	// Subscribed via Apple IAP
                    	iapServerNotificationService.cancelSubscription(programBlockEvent.getProgramId(), programSubscription.getSubscribedViaPlatform().getPlatformTypeId(), programSubscription.getUser());
                    	//
                    } else {
                        // Subscribed via Authorize.net
                        subscriptionService.cancelRecurringProgramSubscription(programBlockEvent.getProgramId(), programSubscription.getSubscribedViaPlatform().getPlatformTypeId(), programSubscription.getUser(), false);
                    }

                }
            }
        }
        // Unpublish/Block From App store by changing Ready For Sale flag as false
        //fitwiseITMSUploadEntityService.unPublish(programBlockEvent.getProgram());
    }

    /**
     * Subscription related events for program unblock
     * @param programBlockEvent
     */
    @Override
    public void programUnblocked(ProgramBlockEvent programBlockEvent) {


    }
}

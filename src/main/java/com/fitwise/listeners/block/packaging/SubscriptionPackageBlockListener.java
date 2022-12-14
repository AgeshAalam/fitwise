package com.fitwise.listeners.block.packaging;

import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.payment.stripe.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Created by Vignesh G on 29/01/21
 */
@Component
public class SubscriptionPackageBlockListener implements PackageBlockListener {

    @Autowired
    SubscriptionService subscriptionService;
    @Autowired
    private StripeService stripeService;

    @Override
    public void packageBlocked(PackageBlockEvent packageBlockEvent) {

        List<PackageSubscription>  packageSubscriptions = subscriptionService.getPaidSubscriptionsOfPackage(packageBlockEvent.getSubscriptionPackageId());
        for (PackageSubscription packageSubscription : packageSubscriptions) {
            if (packageSubscription.isAutoRenewal()) {
                if (packageSubscription.getSubscribedViaPlatform() != null) {
                    if (packageSubscription.getSubscribedViaPlatform().getPlatformTypeId() != 2) {
                        stripeService.cancelStripePackageSubscription(packageBlockEvent.getSubscriptionPackageId(), packageSubscription.getSubscribedViaPlatform().getPlatformTypeId(), packageSubscription.getUser(), false);
                    }
                }
            }
        }

    }

    @Override
    public void packageUnblocked(PackageBlockEvent packageBlockEvent) {

    }
}

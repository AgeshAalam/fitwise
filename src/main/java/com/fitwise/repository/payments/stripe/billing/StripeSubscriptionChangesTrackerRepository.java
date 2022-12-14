package com.fitwise.repository.payments.stripe.billing;

import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionChangesTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/*
 * Created by Vignesh G on 26/10/20
 */
@Repository
public interface StripeSubscriptionChangesTrackerRepository extends JpaRepository<StripeSubscriptionChangesTracker, Long> {

    /**
     * @param orderId
     * @return
     */
    StripeSubscriptionChangesTracker findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(String orderId);

    /**
     * @param subscriptionId
     * @param modifiedDate
     * @return
     */
    List<StripeSubscriptionChangesTracker> findBySubscriptionIdAndIsSubscriptionActiveTrueAndCreatedDateLessThanEqualOrderByModifiedDateDesc(String subscriptionId, Date modifiedDate);

    StripeSubscriptionChangesTracker findTop1BySubscriptionIdOrderByModifiedDateDesc(String subscriptionId);

}

package com.fitwise.repository.payments.authnet;

import com.fitwise.entity.payments.authNet.AuthNetSubscriptionChangesTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AuthNetSubscriptionChangesTrackerRepository extends JpaRepository<AuthNetSubscriptionChangesTracker, Long> {
    AuthNetSubscriptionChangesTracker findTop1BySubscriptionIdOrderByModifiedDateDesc(String subscriptionId);

    AuthNetSubscriptionChangesTracker findTop1ByOrderIdOrderByModifiedDateDesc(String subscriptionId);

    List<AuthNetSubscriptionChangesTracker> findBySubscriptionIdAndCreatedDateLessThanEqualOrderByModifiedDateDesc(String subscriptionId, Date createdDate);

    List<AuthNetSubscriptionChangesTracker> findBySubscriptionIdAndIsSubscriptionActiveTrueAndCreatedDateLessThanEqualOrderByModifiedDateDesc(String subscriptionId, Date createdDate);
}

package com.fitwise.repository.subscription;

import com.fitwise.entity.subscription.FitwiseSubscriptionShare;
import com.fitwise.entity.subscription.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FitwiseSubscriptionShareRepository extends JpaRepository<FitwiseSubscriptionShare, Long> {
    List<FitwiseSubscriptionShare> findBySubscriptionTypeAndActive(final SubscriptionType subscriptionType, final boolean active);
}

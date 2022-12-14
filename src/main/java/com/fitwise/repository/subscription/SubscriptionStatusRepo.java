package com.fitwise.repository.subscription;

import com.fitwise.entity.subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionStatusRepo extends JpaRepository<SubscriptionStatus, Long> {
    SubscriptionStatus findBySubscriptionStatusId(Long subscriptionStatusId);

    SubscriptionStatus findBySubscriptionStatusNameIgnoreCaseContaining(String subscribedState);
}

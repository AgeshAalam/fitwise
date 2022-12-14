package com.fitwise.repository.subscription;

import com.fitwise.entity.User;
import com.fitwise.entity.subscription.FitwiseSubscriptionShareForInstructor;
import com.fitwise.entity.subscription.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FitwiseSubscriptionShareForInstructorRepository extends JpaRepository<FitwiseSubscriptionShareForInstructor, Long> {
    List<FitwiseSubscriptionShareForInstructor> findBySubscriptionTypeAndUserAndActive(final SubscriptionType subscriptionType, final User user, final boolean active);
}

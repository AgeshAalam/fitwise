package com.fitwise.repository.subscription;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.subscription.SubscriptionPlan;


@Repository
public interface SubscriptionPlansRepo extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findAll();

    SubscriptionPlan findBySubscriptionPlanId(long subscriptionPlanId);

    SubscriptionPlan findByDuration(Long duration);
}

package com.fitwise.repository.payments.appleiap;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.payments.appleiap.AppleSubscriptionStatus;

public interface AppleSubscriptionStatusRepository extends JpaRepository<AppleSubscriptionStatus, Long> {
	AppleSubscriptionStatus findBySubscriptionStatusName(String subscriptionStatusName);
}

package com.fitwise.repository;

import com.fitwise.entity.payments.authNet.AuthNetSubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthNetSubscriptionStatusRepository extends JpaRepository<AuthNetSubscriptionStatus, Long> {
    AuthNetSubscriptionStatus findBySubscriptionStatusName(String subscriptionStatusName);
}

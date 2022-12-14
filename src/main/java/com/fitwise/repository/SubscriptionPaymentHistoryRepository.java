package com.fitwise.repository;

import com.fitwise.entity.ProgramSubscriptionPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPaymentHistoryRepository extends JpaRepository<ProgramSubscriptionPaymentHistory,Long> {
}

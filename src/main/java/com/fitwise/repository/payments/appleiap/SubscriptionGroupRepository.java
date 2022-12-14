package com.fitwise.repository.payments.appleiap;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fitwise.entity.payments.appleiap.SubscriptionGroup;

public interface SubscriptionGroupRepository extends JpaRepository<SubscriptionGroup, Long> {
	 @Query("select s.subscriptionGroupName from SubscriptionGroup s")
	 List<String> getAllSubscriptionGroupName();
	 
	 @Query("select s.subscriptionDisplayName from SubscriptionGroup s")
	 List<String> getAllSubscriptionDisplayName();	 
}

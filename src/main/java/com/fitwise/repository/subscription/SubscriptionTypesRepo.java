package com.fitwise.repository.subscription;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.subscription.SubscriptionType;

@Repository
public interface SubscriptionTypesRepo extends JpaRepository<SubscriptionType, Long> {
    List<SubscriptionType> findAll();

    SubscriptionType findBySubscriptionTypeId(long id);

    SubscriptionType findByNameIgnoreCase(String typeName);
}

package com.fitwise.repository.payments.stripe.connect;

import com.fitwise.entity.payments.stripe.connect.StripeAccountAndUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeAccountAndUserMappingRepository extends JpaRepository<StripeAccountAndUserMapping, Long> {
    StripeAccountAndUserMapping findByUserUserId(Long userId);

    StripeAccountAndUserMapping findByStripeAccountId(String accountId);
}

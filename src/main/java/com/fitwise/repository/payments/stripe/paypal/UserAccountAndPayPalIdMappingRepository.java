package com.fitwise.repository.payments.stripe.paypal;

import com.fitwise.entity.payments.paypal.UserAccountAndPayPalIdMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountAndPayPalIdMappingRepository extends JpaRepository<UserAccountAndPayPalIdMapping, Long> {
    UserAccountAndPayPalIdMapping findByUserUserId(Long userId);
}

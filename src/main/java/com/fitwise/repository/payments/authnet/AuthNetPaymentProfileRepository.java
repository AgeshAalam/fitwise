package com.fitwise.repository.payments.authnet;

import com.fitwise.entity.payments.authNet.AuthNetPaymentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthNetPaymentProfileRepository extends JpaRepository<AuthNetPaymentProfile, Long> {
    AuthNetPaymentProfile findByArbCustomerProfileIdAndArbPaymentProfileId(String customerProfileId, String paymentProfileId);
}

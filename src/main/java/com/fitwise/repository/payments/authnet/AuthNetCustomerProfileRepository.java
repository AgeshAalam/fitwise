package com.fitwise.repository.payments.authnet;

import com.fitwise.entity.payments.authNet.AuthNetCustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthNetCustomerProfileRepository extends JpaRepository<AuthNetCustomerProfile, Long> {
    AuthNetCustomerProfile findByUserUserId(Long userId);
}

package com.fitwise.repository.payments.authnet;

import com.fitwise.entity.payments.authNet.AuthNetWebHookLogger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthNetWebHookLoggerRepository extends JpaRepository<AuthNetWebHookLogger, Long> {
}

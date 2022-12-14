package com.fitwise.repository.payments.authnet;

import com.fitwise.entity.payments.authNet.AuthNetPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthNetPaymentRepository extends JpaRepository<AuthNetPayment, Long> {

    AuthNetPayment findByOrderManagementOrderIdOrderByModifiedDateDesc(String orderNumber);

    AuthNetPayment findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(String orderNumber);

    AuthNetPayment findTop1ByArbSubscriptionIdOrderByModifiedDateDesc(String subscriptionId);

    AuthNetPayment findByTransactionIdOrderByModifiedDateDesc(String transactionId);

    AuthNetPayment findTop1ByTransactionId(String transactionId);

    List<AuthNetPayment> findByArbSubscriptionIdAndResponseCode(String arbSubscriptionId, String responseCode);

    List<AuthNetPayment> findByTransactionStatusIgnoreCaseContaining(String transactionStatus);
}

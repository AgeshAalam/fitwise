package com.fitwise.repository.payments.appleiap;


import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.payments.appleiap.ApplePayment;

public interface ApplePaymentRepository extends JpaRepository<ApplePayment, Long> {
    ApplePayment findTop1ByOrderManagementOrderIdOrderByCreatedDateDesc(String orderNumber);
    ApplePayment findTop1ByTransactionIdAndOriginalTransactionIdOrderByModifiedDateDesc(String transactionId, String originalTxnId);
    List<ApplePayment> findByOriginalTransactionIdAndTransactionStatus( String originalTxnId,String status);
    ApplePayment findTop1ByTransactionIdAndOriginalTransactionIdAndTransactionStatusOrderByCreatedDateDesc(String transactionId, String originalTxnId,String status);
    ApplePayment findTop1ByTransactionId(String transactionId);
    List<ApplePayment> findByTransactionStatusAndIsPaymentSettledAndPurchaseDateEqualsOrderByCreatedDateDesc(String status, boolean flag,Date date);
    List<ApplePayment> findByTransactionStatusAndIsPaymentSettled(String status, boolean flag);
}

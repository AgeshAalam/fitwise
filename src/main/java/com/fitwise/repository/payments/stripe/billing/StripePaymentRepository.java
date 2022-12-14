package com.fitwise.repository.payments.stripe.billing;

import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface StripePaymentRepository extends JpaRepository<StripePayment, Long> {

    /**
     * Get latest StripePayment by order id
     *
     * @param orderId
     * @return
     */
    StripePayment findTop1ByOrderManagementOrderIdOrderByModifiedDateDesc(String orderId);

    /**
     * @param orderIdList
     * @param transactionStatus
     * @return
     */
    List<StripePayment> findByOrderManagementOrderIdInAndTransactionStatus(List<String> orderIdList, String transactionStatus);

    /**
     * @param instructorId
     * @param transactionStatus
     * @return
     */
    List<StripePayment> findByOrderManagementProgramOwnerUserIdAndTransactionStatusAndOrderManagementModeOfPayment(Long instructorId, String transactionStatus, String modeOfPayment);

    /**
     * Used to get the list of records based on subscription id and transaction status
     *
     * @param subscriptionId
     * @param transactionStatus
     * @return
     */
    List<StripePayment> findBySubscriptionIdAndTransactionStatus(String subscriptionId, String transactionStatus);

    /**
     * @param subscriptionId
     * @param transactionStatus
     * @param createdDate
     * @return
     */
    List<StripePayment> findBySubscriptionIdAndTransactionStatusAndCreatedDateLessThanEqual(String subscriptionId, String transactionStatus, Date createdDate);


    /**
     * Get the latest record in the table based on charge id
     *
     * @param chargeId
     * @return
     */
    StripePayment findTop1ByChargeId(String chargeId);


    /**
     * Used to get the latest record based on invoice and charge id and status
     *
     * @param invoiceId
     * @param chargeId
     * @return
     */
    StripePayment findTop1ByInvoiceIdAndChargeId(String invoiceId, String chargeId);

    StripePayment findTop1ByOrderManagementAndTransactionStatusOrderByModifiedDateDesc(final OrderManagement orderManagement, final String transactionStatus);

    StripePayment findTop1ByOrderManagementOrderByModifiedDateDesc(final OrderManagement orderManagement);
}

package com.fitwise.entity.payments.stripe.billing;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.payments.common.OrderManagement;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class StripePayment extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderManagement orderManagement;

    // Invoice id from Stripe
    private String invoiceId;

    //Equivalent to Transaction id
    // Used for refunding the transaction
    private String chargeId;

    private String transactionStatus;

    @ManyToOne
    @JoinColumn(name = "invoice_number")
    private InvoiceManagement invoiceManagement;

    private String errorStatusCode;

    private String errorCode;

    private String errorMessage;

    private String declinedCode;

    private String subscriptionId;

    private Double amountPaid;

    private Double amountRefunded;

    private String refundTransactionId;

    private Boolean isRefundUnderProcessing;
}

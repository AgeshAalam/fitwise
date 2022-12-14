package com.fitwise.entity.payments.authNet;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.payments.common.OrderManagement;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


/**
 * Table that contains the AuthNet payment Transaction details
 */
@Entity
@Getter
@Setter
public class AuthNetPayment extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderManagement orderManagement; // Will be sent as reference key in AuthNet request during transaction initiation

    private String responseCode; // Response code from AuthNet for each transaction

    private String transactionId; // Transaction ref id from AuthNet

    private String transactionStatus;

    private String receiptNumber; // Fitwise generated InvoiceNumber

    private String errorCode;

    private String errorMessage;

    private Boolean isARB;

    private String arbSubscriptionId;

    private Boolean isARBUnderProcessing = false;

    private String refundTransactionId;

    private Double amountPaid;

    private Double amountRefunded;

    private Boolean isRefundUnderProcessing = false;

    private Boolean isDomesticCard = false;
    
    private String userCardCountryCode;

}

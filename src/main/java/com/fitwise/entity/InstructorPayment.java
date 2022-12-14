package com.fitwise.entity;

import com.fitwise.entity.payments.common.OrderManagement;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * This entity will provide all the split for the program price
 * Instructor share
 * Provider charge(CC, Apple)
 * Trainnr share
 */
@Entity
@Getter
@Setter
public class InstructorPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long instructorPaymentId;

    private String billNumber;

    private String variableBillNumber;

    private String fixedBillNumber;

    private double totalAmt;

    private double instructorShare;

    private double fitwiseShare;

    private double providerCharge;

    private double fixedCharge;

    private double priceOnPurchase;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private OrderManagement orderManagement;

    private Date dueDate;

    private String cardType;

    /**
     * To check whether payout is successfully processed out
     */
    private Boolean isTransferDone = false;

    /**
     * This denotes the stripe transfer transaction id
     */
    private String stripeTransferId;

    /**
     * Denotes how the payout was processed. Whether through Stripe/Manually
     */
    private String transferMode;

    /**
     * To denote whether a payout is attempted to pay customer.
     * If this is true and payoutDone is false, then payout got failure
     * If this is false, then payout is yet to be done
     */
    private Boolean isTransferAttempted = false;

    /**
     * This denotes the stripe transfer status.
     * Status are created, failed, updated, paid, reversed.
     * Will be Updated through webhook.
     */
    private String stripeTransferStatus;

    /**
     * For apple payments, top up needs to be initiated.
     * This will bring the amount from Fitwise bank account to Fitwise Stripe account.
     * Once the amount is available in fitwise stripe account, transaction to the instructors will be processed.
     */
    private Boolean isTopUpInitiated = false;

    /**
     * To check whether a transfer was failed
     */
    private Boolean isTransferFailed = false;

    private Date transferDate;

    @ManyToOne
    private User instructor;

    private Double flatTax;
}

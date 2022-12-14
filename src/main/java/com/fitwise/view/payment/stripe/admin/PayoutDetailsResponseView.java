package com.fitwise.view.payment.stripe.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayoutDetailsResponseView {
    private String transferAmount;
    private String transferDueDate;
    private String transferStatus;
    private String payoutTransactionId;
    private String transferPaidDate;
    private String transferBillNumber;
    private String transferVariableBillNumber;
    private String transferFixedBillNumber;
    private String transferProviderCharge;
    private String transferFixedCharge;
    private String transferMode;
    private String memberName;
    private String instructorName;
    private String programName;
    private String purchasedAmount;
    private String subscribedDate;
    private String subscribedViaPlatform;
    private String purchaseTransactionId;
    private String subscriptionType;
    private String payoutPaidVia;
    private String email;
}

package com.fitwise.view.payment.stripe.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PayoutsTileResponseView {
    private Long instructorPaymentId;
    private String instructorName;
    private String instructorShare;
    private String instructorShareFormatted;
    private String dueDate;
    private Date dueDateTimeStamp;
    private String status;
    private String transactionId;
    private String instructorPayoutMode;
    private String subscribedViaPlatform;
    private String paidDate;
    private String failureMessage;
    private String subscriptionType;
    private String payoutPaidVia;
    private String transferBillNumber;
    private String email;
}

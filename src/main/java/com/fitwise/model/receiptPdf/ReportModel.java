package com.fitwise.model.receiptPdf;

import com.fitwise.constants.KeyConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportModel {
    private String addressLine1 = "-";
    private String addressLine2 = "-";
    private String addressLine3 = "-";
    private String userEmail = "-";
    private String userPostalCode = "-";
    private String invoiceNumber;
    private String invoiceDate;
    private String invoiceAmount;
    private String invoicePaymentTerms;
    private String orderId;
    private String orderStatus;
    private String purchasedDate;
    private String transactionId;
    private String instructorName;
    private String programName;
    private String programDuration;
    private String autoRenewal = KeyConstants.KEY_NO;
    private String nextRenewalOn = "-";
    private int totalRenewalCount;
    private int currentSubscriptionRenewalCount;
    private String billedToName;
    private String programDescription = "-";
    private String programTerm;
    private String programPrice;
    private String paymentNoteAmount;
    private String paymentNote;
    private String offerName;
    private String offerPrice;
    private String totalAmount;
}

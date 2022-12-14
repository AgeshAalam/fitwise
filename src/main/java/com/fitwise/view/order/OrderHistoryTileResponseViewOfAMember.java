package com.fitwise.view.order;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class OrderHistoryTileResponseViewOfAMember {
    private String programName;
    private String instructorName;
    private String duration;
    private boolean isAutoRenewal = false;
    private int totalRenewalCount;
    private int currentSubscriptionRenewalCount;
    private String nextRenewalDate;
    private Date nextRenewalDateTimeStamp;
    private String orderId;
    private String orderStatus;
    private String purchasedDate;
    private Date purchasedDateTimeStamp;
    private String downloadReceiprtUrl = "";
    private String transactionId;
    private Double programPrice;
    private String formattedProgramPrice;
    private Double orderTotalPrice;
    private String formattedOrderTotalPrice;
    private OrderDiscountDetails orderDiscountDetails;
    private Date settlementDate;
    private boolean canRefund = false;
    private String platformName = "iOS";
    private Boolean isTransactionRefunded = false;
    private String refundTransactionId;
    private Double refundedAmount;
    private String formattedRefundAmount;
    private String refundedDate;
    private Date refundedDateTimeStamp;
    private String refundReason;
    private String paymentGateway;
}

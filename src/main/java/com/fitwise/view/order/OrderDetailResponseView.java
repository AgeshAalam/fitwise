package com.fitwise.view.order;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class OrderDetailResponseView {
    private String programName;
    private Long programId;
    private String thumbnailUrl;
    private String orderId;
    private String orderStatus;
    // This will be always false in case of authorize.net since we get immediate success/failure response from authorize.net
    private Boolean isOrderUnderProcessing = false;
    private String purchasedDate;
    private Date purchasedDateTimeStamp;
    private String transactionId;
    private String price;
    private String formattedPrice;
    private String orderTotal;
    private String formattedOrderTotal;
    private OrderDiscountDetails orderDiscountDetails;
    private String instructorName;
    private String duration;
    private boolean isAutoRenewal = false;
    private int totalRenewalCount;
    private int currentSubscriptionRenewalCount;
    private String currentStatus;
    private String nextRenewalDate;
    private Date nextRenewalDateTimeStamp;
    private boolean doHaveReceiptURl = true;
    private boolean isOrderSuccess = false;
    private Long subscribedViaPlatformId;
    private Boolean isTransactionRefunded = false;
    private Double refundedAmount;
    private String formattedRefundAmount;
    private Date refundedDateTimeStamp;
    private String refundedDate;
    private String refundReason;
    private String refundTransactionId;
    private String paymentGateWay;
}

package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
 * Created by Vignesh.G on 28/06/21
 */
@Setter
@Getter
@AllArgsConstructor
public class PayoutDao {

    private Long instructorPaymentId;
    private String instructorName;
    private double instructorShare;
    private String subscriptionType;
    private Long instructorId;
    private Date dueDate;
    private String stripeTransferStatus;
    private String stripeTransferId;
    private Boolean isTransferDone;
    private Boolean isTopUpInitiated;
    private Boolean isTransferFailed;
    private String orderStatus;
    private String orderId;
    private Long stripeAccountId;
    private String platform;
    private Date transferDate;
    private String transferError;
    private String transferMode;
    private String billNumber;
    private String email;


}

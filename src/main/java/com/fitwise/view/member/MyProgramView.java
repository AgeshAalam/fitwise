package com.fitwise.view.member;

import lombok.Data;

import java.util.Date;

/*
 * Created by Vignesh G on 25/04/20
 */
@Data
public class MyProgramView {

    private long programId;

    private String title;

    private String programThumbnail;

    private String progress;

    private long progressPercent;

    private String duration;

    private String subscriptionStatus;

    private String startDate;

    private Date startDateTimeStamp;

    private Date subscribedDate;
    private String subscribedDateFormatted;

    private Date completedDate;
    private String completedDateFormatted;

    private String orderStatus;

    private Date subscriptionExpiry;
    private String subscriptionExpiryFormatted;

    // This will be always false in case of authorize.net since we get immediate success/failure response from authorize.net
    private Boolean isOrderUnderProcessing = false;

    private int numberOfCurrentAvailableOffers;

    private String subscriptionType;

    private boolean isFreeToAccess = false;

}

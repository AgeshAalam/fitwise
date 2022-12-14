package com.fitwise.response.packaging;

import lombok.Data;

import java.util.Date;

@Data
public class MemberPackageTileView {

    private long subscriptionPackageId;

    private String title;

    private long imageId;

    private String imageUrl;

    private String duration;

    private int noOfPrograms;

    private int sessionCount;

    private String subscriptionStatus;

    private Date subscribedDate;

    private String subscribedDateFormatted;

    private String orderStatus;

    private Date subscriptionExpiry;

    private String subscriptionExpiryFormatted;

    private boolean isAutoSubscriptionOn;

    private int numberOfCurrentAvailableOffers;

    private boolean freeToAccess = false;
}

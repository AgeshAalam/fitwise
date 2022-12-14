package com.fitwise.view.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
 * Created by Vignesh G on 28/01/21
 */
@Getter
@Setter
public class MemberPackageHistoryView {

    private long subscriptionPackageId;

    private String title;

    private String imageUrl;

    private String duration;

    private int noOfPrograms;

    private int sessionCount;

    private String subscriptionStatus;

    private Date subscribedDate;

    private String subscribedDateFormatted;

    private Date initialStartDate;

    private String initialStartDateFormatted;
}

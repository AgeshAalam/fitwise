package com.fitwise.view.member;

import lombok.Data;

import java.util.Date;

/*
 * Created by Vignesh G on 25/04/20
 */
@Data
public class TodaysProgramView {

    private long programId;

    private String title;

    private String programThumbnail;

    private String progress;

    private long progressPercent;

    private String startDate;
    private Date startDateTimeStamp;

    private String subscriptionStatus;

    private boolean freeToAccess = false;

}

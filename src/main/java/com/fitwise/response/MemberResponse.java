package com.fitwise.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class MemberResponse {

    private long userId;
    private String memberName;
    private String email;
    private String imageUrl;
    private double amountSpent;
    private String amountSpentFormatted;
    private long totalSubscription;
    private long completedProgram;
    private Date lastAccess;
    private String lastAccessFormatted;
    private boolean isBlocked;
    private String status;
    private double amountSpentOnProgram;
    private String amountSpentOnProgramFormatted;
    private double amountSpentOnPackage;
    private String amountSpentOnPackageFormatted;
    private long packageSubscriptionCount;
    private Date onboardedDate;
    private String onboardedDateFormatted;
}

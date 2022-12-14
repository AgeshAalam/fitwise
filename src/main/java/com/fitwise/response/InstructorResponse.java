package com.fitwise.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class InstructorResponse {

    private long userId;
    private String instructorName;
    private String imageUrl;
    private double upcomingPayment;
    private String upcomingPaymentFormatted;
    private double programOutstandingPayment;
    private String programOutstandingPaymentFormatted;
    private long totalSubscription;
    private long publishedProgram;
    private String status;
    private boolean isBlocked;
    private Date onboardedDate;
    private String onboardedDateFormatted;
    private long totalExercises;
    private double packageOutstandingPayment;
    private String packageOutstandingPaymentFormatted;
    private long packageSubscriptionCount;
    private long publishedPackageCount;
    private String onboardedPaymentMode;
    private Date lastAccess;
    private String lastAccessFormatted;
    private String email;
    private String tier;

}

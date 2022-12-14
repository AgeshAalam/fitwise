package com.fitwise.view.admin;

import lombok.Data;

import java.util.Date;

@Data
public class PackageResponse {

    private Long subscriptionPackageId;

    private String title;

    private String instructorName;

    private int programCount;

    private int sessionCount;

    private int activeSubscriptions;

    private Date publishedDate;

    private String publishedDateFormatted;

    private boolean isBlocked;

}

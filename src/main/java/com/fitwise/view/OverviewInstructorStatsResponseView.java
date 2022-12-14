package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OverviewInstructorStatsResponseView {
    private String userName;
    private String expectedRevenue;
    private String overAllRevenue;
    private int totalSubscriptions;
    private int totalPrograms;
    private float overallRatings;
}

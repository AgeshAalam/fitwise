package com.fitwise.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProgramAnalytics {

    private String programType;
    private long numberOfPrograms;
    private long subscriptions;
    private double revenue;
    private String formattedRevenue;
}

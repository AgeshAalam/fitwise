package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyRevenueView {
    private int weekId;
    private String weekName;
    private double revenue;
    private String formattedRevenue;
    private String weekDuration;
}

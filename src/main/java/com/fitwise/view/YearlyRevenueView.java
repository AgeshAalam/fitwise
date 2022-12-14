package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YearlyRevenueView {
    private int monthId;
    private String monthName;
    private double monthRevenue;
    private String formattedMonthRevenue;
}

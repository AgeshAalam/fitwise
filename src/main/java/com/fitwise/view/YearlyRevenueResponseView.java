package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YearlyRevenueResponseView {
    private String totalYearlyRevenue;
    private List<YearlyRevenueView> monthlyRevenueSplits;
}

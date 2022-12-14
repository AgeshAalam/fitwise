package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MonthlyRevenueResponseView {
    private String totalMonthlyRevue;
    private List<MonthlyRevenueView> revenuePerWeek;
}

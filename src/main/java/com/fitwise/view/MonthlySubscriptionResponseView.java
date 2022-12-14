package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MonthlySubscriptionResponseView {
    private int totalMonthlySubscriptions;
    private List<MonthlySubscriptionView> subscriptionsPerWeek;
}

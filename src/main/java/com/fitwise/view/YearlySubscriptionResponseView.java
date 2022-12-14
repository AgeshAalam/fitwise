package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YearlySubscriptionResponseView {
    private int totalYearlySubscriptions;
    private List<YearlySubscriptionView> monthlySubscriptionSplits;
}

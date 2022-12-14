package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlySubscriptionView {
    private int weekId;
    private String weekName;
    private int subscriptionCount;
    private String weekDuration;
}

package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class YearSubscriptionsOfNewAndRenewalView {
    private int entryId;
    private String entryName;
    private int newSubscriptionCount;
    private int renewalSubscriptionCount;
}

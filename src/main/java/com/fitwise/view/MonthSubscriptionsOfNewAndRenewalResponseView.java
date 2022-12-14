package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MonthSubscriptionsOfNewAndRenewalResponseView {

    private int totalCount;
    private List<MonthSubscriptionsOfNewAndRenewalView> chartEntryList;
}

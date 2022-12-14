package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class YearSubscriptionsOfNewAndRenewalResponseView {

    private int totalCount;
    private List<YearSubscriptionsOfNewAndRenewalView> chartEntryList;
}

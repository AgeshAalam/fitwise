package com.fitwise.view.calendar;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubscriptionPackageScheduleAvailability {

    private Long subscriptionPackageId;

    private String subscribedDate;

    private String expiryDate;

    private Long packageSessionMappingId;

    private List<String> restrictedDates;

}

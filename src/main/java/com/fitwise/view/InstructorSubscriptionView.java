package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InstructorSubscriptionView {

    private Long user_id;

    private boolean isAutoRenewal;

    private Long instructor_id;

    private Long subscription_plan_id;

    private Long subscriptionStatusId;

    private Long devicePlatformId;
}

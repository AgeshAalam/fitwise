package com.fitwise.specifications.jpa.dao;

import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.payments.appleiap.AppleSubscriptionStatus;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManageSubscriptionForProgramWithStripe {

    private PlatformType subscribedViaPlatform;

    private Date subscribedDate;

    private Programs program;

    private OrderManagement orderManagement;

    private StripeSubscriptionStatus stripeSubscriptionStatus;

    private AppleSubscriptionStatus appleSubscriptionStatus;
}

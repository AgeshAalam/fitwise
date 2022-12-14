package com.fitwise.specifications.jpa.dao;

import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.subscription.SubscriptionPlan;
import com.fitwise.entity.subscription.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class ManageSubscriptionDAOForPackage {

    private PlatformType subscribedViaPlatform;

    private Date subscribedDate;

    private SubscriptionPackage subscriptionPackage;

    private OrderManagement orderManagement;

    private long programCount;

    private long sessionCount;
}

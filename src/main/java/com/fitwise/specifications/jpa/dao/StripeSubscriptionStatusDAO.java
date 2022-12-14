package com.fitwise.specifications.jpa.dao;

import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class StripeSubscriptionStatusDAO {

    private Long programId;

    private StripeSubscriptionStatus subscriptionStatus;

    private Date modifiedDate;
}

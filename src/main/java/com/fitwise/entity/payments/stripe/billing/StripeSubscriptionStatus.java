package com.fitwise.entity.payments.stripe.billing;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class StripeSubscriptionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionStatusId;

    private String subscriptionStatus;

}

package com.fitwise.entity.payments.stripe;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.User;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 18/12/20
 */
@Entity
@Getter
@Setter
public class StripeSubscriptionAndUserPackageMapping extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    @JoinColumn(name = "subscription_package_id")
    private SubscriptionPackage subscriptionPackage;

    private String stripeSubscriptionId;

    @ManyToOne
    private StripeSubscriptionStatus subscriptionStatus;

}

package com.fitwise.entity.payments.stripe;

import com.fitwise.entity.packaging.SubscriptionPackage;
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
public class StripeProductAndPackageMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stripeProductMappingId;

    @ManyToOne
    @JoinColumn(name = "subscription_package_id")
    private SubscriptionPackage subscriptionPackage;

    private String stripeProductId;

    private boolean isActive;

}

package com.fitwise.entity.payments.stripe;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Date;

/*
 * Created by Vignesh G on 24/11/20
 */
@Entity
@Setter
@Getter
public class StripeSubscriptionAndCouponMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private StripeCouponAndOfferCodeMapping stripeCouponAndOfferCodeMapping;

    private String stripeSubscriptionId;

    private Date subscriptionStartDate;

    private Date couponEndDate;

}

package com.fitwise.entity.payments.stripe;

import com.fitwise.entity.discounts.OfferCodeDetail;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/*
 * Created by Vignesh G on 17/11/20
 */
@Entity
@Setter
@Getter
public class StripeCouponAndOfferCodeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private OfferCodeDetail offerCodeDetail;

    private String stripeCouponId;

}

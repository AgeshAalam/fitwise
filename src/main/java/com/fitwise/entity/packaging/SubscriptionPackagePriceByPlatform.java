package com.fitwise.entity.packaging;

import com.fitwise.entity.PlatformWiseTaxDetail;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 23/09/20
 */
@Entity
@Data
public class SubscriptionPackagePriceByPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double price;

    @ManyToOne
    private SubscriptionPackage subscriptionPackage;

    @ManyToOne
    private PlatformWiseTaxDetail platformWiseTaxDetail;

}

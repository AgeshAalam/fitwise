package com.fitwise.entity.packaging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.discounts.DiscountLevel;
import com.fitwise.entity.discounts.OfferCodeDetail;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
@Data
public class PackageOfferMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packageOfferMappingId;

    @OneToOne
    @JoinColumn(name = "instructorId")
    private User instructor;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name="discountLevelId")
    private DiscountLevel discountLevel;


    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "offerId")
    private OfferCodeDetail offerCodeDetail;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinColumn(name = "subscriptionPackageId")
    private SubscriptionPackage subscriptionPackage;

    private Boolean needDiscountUpdate;

    private String discountStatus;

    private Boolean needMailUpdate;

}

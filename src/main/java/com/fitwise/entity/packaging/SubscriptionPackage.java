package com.fitwise.entity.packaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.Images;
import com.fitwise.entity.PackageDuration;
import com.fitwise.entity.ProgramPrices;
import com.fitwise.entity.Promotions;
import com.fitwise.entity.TimeSpan;
import com.fitwise.entity.User;

import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh G on 18/09/20
 */
@Entity
@Getter
@Setter
public class SubscriptionPackage extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionPackageId;

    @ManyToOne(cascade = CascadeType.DETACH)
    private User owner;

    private String title;

    @Column(length = 45)
    private String shortDescription;

//    @Column(length = 2000)
    @Lob
    @Column( length = 2000 )
    private String description;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.DETACH})
    @JoinColumn(name = "image_id")
    private Images image;

    @OneToOne(cascade = CascadeType.DETACH)
    private PackageDuration packageDuration;

    @OneToMany(mappedBy = "subscriptionPackage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PackageProgramMapping> packageProgramMapping = new ArrayList<>();

    @OneToMany(mappedBy = "subscriptionPackage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PackageKloudlessMapping> packageKloudlessMapping = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private TimeSpan cancellationDuration;

    private boolean isRestrictedAccess;

    @OneToMany(mappedBy = "subscriptionPackage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PackageMemberMapping> packageMemberMapping = new ArrayList<>();

    @OneToMany(mappedBy = "subscriptionPackage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PackageExternalClientMapping> externalMemberMapping = new ArrayList<>();

    private String clientMessage;

    @OneToMany(mappedBy = "subscriptionPackage", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    private List<SubscriptionPackagePriceByPlatform> packagePriceByPlatforms = new ArrayList<>();

    private String status;

    private String postCompletionStatus;

    private Double price;

    @OneToOne
    @JoinColumn(name = "package_price_id")
    private ProgramPrices packagePrice;

    private Date publishedDate;

    @JsonIgnore
    @OneToMany(mappedBy = "subscriptionPackage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<PackageOfferMapping> packageOfferMappings;

    /** The promotion. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "promotion_id")
    private Promotions promotion;

}

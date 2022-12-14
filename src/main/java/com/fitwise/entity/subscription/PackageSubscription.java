package com.fitwise.entity.subscription;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.User;
import com.fitwise.entity.packaging.SubscriptionPackage;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * Created by Vignesh G on 17/12/20
 */
@Entity
@Getter
@Setter
@Table(name = "package_subscription", indexes = {
        @Index(name = "index_id", columnList = "id", unique = true),
        @Index(name = "index_user_id", columnList = "id,user_id")
})
public class PackageSubscription extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "subscription_package_id")
    private SubscriptionPackage subscriptionPackage;

    @Column(name = "subscribed_date")
    private Date subscribedDate;

    @ManyToOne
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @ManyToOne
    @JoinColumn(name = "subscription_status_id")
    private SubscriptionStatus subscriptionStatus;

    private boolean isAutoRenewal;

    @ManyToOne
    @JoinColumn(name = "platform_id")
    private PlatformType subscribedViaPlatform;

    @OneToMany(mappedBy = "packageSubscription", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PackageProgramSubscription> packageProgramSubscription = new ArrayList<PackageProgramSubscription>();

}
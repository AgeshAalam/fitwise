package com.fitwise.entity.subscription;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

/*
 * Created by Vignesh G on 06/01/21
 */
@Entity
@Getter
@Setter
public class PackageProgramSubscription extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private PackageSubscription packageSubscription;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Programs program;

    @Column(name = "subscribed_date")
    private Date subscribedDate;

    @ManyToOne
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @ManyToOne
    @JoinColumn(name = "platform_id")
    private PlatformType subscribedViaPlatform;

    @ManyToOne
    @JoinColumn(name = "subscription_status_id")
    private SubscriptionStatus subscriptionStatus;

}
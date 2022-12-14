package com.fitwise.entity.subscription;

import com.fitwise.entity.PlatformType;
import com.fitwise.entity.ProgramSubscriptionPaymentHistory;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "subscription_audit", indexes = {
        @Index(name = "index_id", columnList = "audit_id", unique = true),
        @Index(name = "index_user", columnList = "audit_id, user_id"),
        @Index(name = "index_program_subscription", columnList = "program_subscription_id"),
        @Index(name = "index_package_subscription", columnList = "package_subscription_id")
})
public class SubscriptionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "subscription_type_id")
    private SubscriptionType subscriptionType;

    @ManyToOne
    @JoinColumn(name = "program_subscription_id")
    private ProgramSubscription programSubscription;

    @ManyToOne
    @JoinColumn(name = "instructor_subscription_id")
    private InstructorSubscription instructorSubscription;

    @ManyToOne
    @JoinColumn(name = "package_subscription_id")
    private PackageSubscription packageSubscription;

    @ManyToOne
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @ManyToOne
    @JoinColumn(name = "subscription_status_id")
    private SubscriptionStatus subscriptionStatus;

    @ManyToOne
    @JoinColumn(name = "platform_id")
    private PlatformType subscribedViaPlatform;

    private Date subscriptionDate;

    private boolean isAutoRenewal;

    /**
     * Will be used to log whether the subscription is a new one or a already subscribed one is being renewed
     */
    private String renewalStatus;

    @Column(name = "created_date", updatable = false)
    @CreationTimestamp
    private Date createdDate;

    @OneToOne
    @JoinColumn(name = "revenue_history_id")
    private ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory;
    
    
    @ManyToOne
    @JoinColumn(name = "tier_subscription_id")
    private TierSubscription tierSubscription;
}
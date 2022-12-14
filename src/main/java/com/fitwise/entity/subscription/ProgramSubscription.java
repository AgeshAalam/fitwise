package com.fitwise.entity.subscription;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "program_subscription", indexes = {
        @Index(name = "index_id", columnList = "program_subscription_id", unique = true),
        @Index(name = "index_user_id", columnList = "program_subscription_id, user_id")
})
public class ProgramSubscription extends AuditingEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_subscription_id")
    private Long programSubscriptionId;

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
    @JoinColumn(name = "subscription_status_id")
    private SubscriptionStatus subscriptionStatus;

    private boolean isAutoRenewal;

    @ManyToOne
    @JoinColumn(name = "platform_id")
    private PlatformType subscribedViaPlatform;
    
}
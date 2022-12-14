package com.fitwise.entity.subscription;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class InstructorSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long instructorSubscriptionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @Column(name = "subscribed_date", updatable = false)
    @CreationTimestamp
    private Date subscribedDate;

    @ManyToOne
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @ManyToOne
    @JoinColumn(name = "subscription_status_id")
    private SubscriptionStatus subscriptionStatus;

    private boolean isAutoRenewal;

}

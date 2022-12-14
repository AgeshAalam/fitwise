package com.fitwise.entity.payments.common;

import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.subscription.SubscriptionType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import java.util.Date;

/**
 * The class which consists of complete data of Fitwise order
 */
@Entity
@Getter
@Setter
public class OrderManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String orderId;

    private String description;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "program_id")
    private Programs program;

    @ManyToOne
    @JoinColumn(name = "subscription_package_id")
    private SubscriptionPackage subscriptionPackage;

    @ManyToOne
    @JoinColumn(name = "subscription_type_id")
    private SubscriptionType subscriptionType;

    private String modeOfPayment;

    private Boolean isAutoRenewable;

    @ManyToOne
    @JoinColumn(name = "platform_id")
    private PlatformType subscribedViaPlatform;

    @CreationTimestamp
    private Date createdDate;

    private String orderStatus;

    private Date settlementDateToInstructor;

    @ManyToOne
    @JoinColumn(name = "tier_id")
    private Tier tier;

    private Double tierPaidAmt;

    private Double tierAdjustedAmt;
}

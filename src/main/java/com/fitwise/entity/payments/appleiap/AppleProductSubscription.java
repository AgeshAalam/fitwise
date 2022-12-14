package com.fitwise.entity.payments.appleiap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;

import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class AppleProductSubscription extends AuditingEntity{

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "program_id")
    private Programs program;

    private String originalTransactionId;
    
    private String appleSubscriptionId; // The subscription id which we get from Apple (webOrderLineItemId)
    
    private String transactionId; 
    
    // To get number of auto renewal count. If event string is 'Renewal' it will be considered as renewal count.
    private String event;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "subscription_status_name")
    private AppleSubscriptionStatus appleSubscriptionStatus;

    @ManyToOne
    @JoinColumn(name = "platform_id")
    private PlatformType subscribedViaPlatform;
}

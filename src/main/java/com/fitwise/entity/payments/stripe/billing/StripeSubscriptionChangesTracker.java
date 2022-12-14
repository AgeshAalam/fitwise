package com.fitwise.entity.payments.stripe.billing;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.payments.common.OrderManagement;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class StripeSubscriptionChangesTracker extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private OrderManagement orderManagement;

    private String subscriptionId;

    private Boolean isSubscriptionActive;
}

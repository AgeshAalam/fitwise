package com.fitwise.entity.payments.stripe.billing;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class StripeSubscriptionAndUserProgramMapping extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Programs program;

    private String stripeSubscriptionId;

    @ManyToOne
    private StripeSubscriptionStatus subscriptionStatus;

}

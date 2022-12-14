package com.fitwise.entity.payments.stripe.billing;

import com.fitwise.entity.AuditingEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class StripeInvoiceAndSubscriptionMapping extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stripeInvoiceAndSubscriptionMappingId;

    private String stripeInvoiceId;

    private String stripeSubscriptionId;

    private String chargeId;

}

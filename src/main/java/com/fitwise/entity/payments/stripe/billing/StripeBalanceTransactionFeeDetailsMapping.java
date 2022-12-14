package com.fitwise.entity.payments.stripe.billing;

import com.fitwise.entity.AuditingEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class StripeBalanceTransactionFeeDetailsMapping extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private StripeBalanceTransaction stripeBalanceTransaction;

    private Double amount;

    private String description;

    private String type;

    private String currency;
}

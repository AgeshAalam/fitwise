package com.fitwise.entity.payments.stripe.billing;

import com.fitwise.entity.AuditingEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class StripeBalanceTransaction extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String balanceTransactionId;

    private Double amount;

    private Long availableOn;

    private Long createdOn;

    private String currency;

    private Double fee;

    // The amount which fitwise receives after all deductions
    private Double netAmount;

    private String status;

    private String source;

    private String type;

    @ManyToOne
    private StripePayout stripePayout;

}

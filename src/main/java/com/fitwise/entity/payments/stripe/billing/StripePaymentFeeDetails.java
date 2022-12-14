package com.fitwise.entity.payments.stripe.billing;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
/**
 * Fees cut off for the stripe payment
 */
public class StripePaymentFeeDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stripePaymentFeeDetailsId;

    private Long amount;

    private String currency;

    private String description;

    private String type;

    @ManyToOne
    @JoinColumn(name = "stripe_payment_id")
    private StripePayment stripePayment;

    @ManyToOne
    @JoinColumn(name = "stripe_payout_id")
    private StripePayout stripePayout;
}

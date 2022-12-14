package com.fitwise.entity.qbo;

import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity for managing qbo refund with fitwise refund
 */
@Entity
@Getter
@Setter
public class QboRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseQboRefundId;

    private String qboRefundReceiptId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private AuthNetPayment authNetPayment;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ApplePayment applePayment;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private StripePayment stripePayment;

    private Boolean needUpdate;

    private String updateStatus;
}

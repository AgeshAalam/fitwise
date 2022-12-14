package com.fitwise.entity.qbo;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity for managing qbo payment with fitwise payment
 */
@Entity
@Getter
@Setter
public class QboPayment extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseQboPaymentId;

    private String qboPaymentId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private AuthNetPayment authNetPayment;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ApplePayment applePayment;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private StripePayment stripePayment;

    private Boolean needUpdate;

    private String updateStatus;

    /**
     * Processing fees for Appstore or Credit card
     */
    private Boolean isCCBillPaymentCreated;


    /**
     * Processing fees for Credit card fixed charge
     */
    private Boolean isFixedCCBillPaymentCreated;

    private String ccBillPaymentId;

    private String ccFixedBillPaymentId;

}

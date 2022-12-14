package com.fitwise.entity.payments.stripe.connect;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.InstructorPayment;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class AppleSettlementByStripe extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private InstructorPayment instructorPayment;

    private String stripeTopUpStatus;

    private String stripeTopUpId;

    private String errorCode;

    private String errorMessage;

    private String stripeErrorCode;

    private String stripeErrorMessage;
}

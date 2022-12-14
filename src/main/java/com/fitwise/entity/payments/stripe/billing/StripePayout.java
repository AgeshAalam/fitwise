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
public class StripePayout extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String payoutId;

    private Long arrivalDateTimeStamp;

    private String balanceTransactionId;

    private Double amount;

    private String currency;

    private String destination;

    private String failureCode;

    private String failureMessage;

    private String status;

    private String type;

    private String eventType;
}

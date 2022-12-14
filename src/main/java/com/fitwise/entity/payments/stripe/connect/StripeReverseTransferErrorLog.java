package com.fitwise.entity.payments.stripe.connect;

import com.fitwise.entity.AuditingEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/*
 * Created by Vignesh G on 05/01/21
 */
@Entity
@Getter
@Setter
public class StripeReverseTransferErrorLog extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stripeTransferId;

    private Long reverseTransferMappingId;

    private String errorCode;

    @Column(length = 2000)
    private String errorMessage;

    private String stripeErrorCode;

    @Column(length = 2000)
    private String stripeErrorMessage;

}

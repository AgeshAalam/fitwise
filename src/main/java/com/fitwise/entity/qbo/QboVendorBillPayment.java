package com.fitwise.entity.qbo;

import com.fitwise.entity.AuditingEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity for managing qbo vendor credit with fitwise refund
 */
@Entity
@Getter
@Setter
public class QboVendorBillPayment extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseQboBillPaymentId;

    private String qboBillPaymentId;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private QboBill qboBill;

    private BigDecimal settlementAmt;

}

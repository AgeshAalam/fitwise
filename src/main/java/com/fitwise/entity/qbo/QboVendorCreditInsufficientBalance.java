package com.fitwise.entity.qbo;

import com.fitwise.entity.payments.credits.InstructorPaymentCreditAudit;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity for managing qbo vendor credit with fitwise refund
 */
@Entity
@Getter
@Setter
public class QboVendorCreditInsufficientBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseQboVendorCreditId;

    private String qboVendorCreditId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private InstructorPaymentCreditAudit instructorPaymentCreditAudit;

    private Boolean needUpdate;

    private String updateStatus;
}

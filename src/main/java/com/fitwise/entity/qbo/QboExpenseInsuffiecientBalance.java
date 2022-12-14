package com.fitwise.entity.qbo;

import com.fitwise.entity.payments.credits.InstructorPaymentCreditAudit;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity for managing qbo expense for vendor insufficient balance
 */
@Entity
@Getter
@Setter
public class QboExpenseInsuffiecientBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseRefundExpenseId;

    private String qboPurchaseId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private InstructorPaymentCreditAudit instructorPaymentCreditAudit;

    private Boolean needUpdate;

    private String updateStatus;
}

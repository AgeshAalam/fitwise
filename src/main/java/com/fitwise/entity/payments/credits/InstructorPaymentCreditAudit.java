package com.fitwise.entity.payments.credits;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.InstructorPayment;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 13/01/21
 */
@Entity
@Getter
@Setter
public class InstructorPaymentCreditAudit extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isCredit;

    private Double amount;

    private Double currentTotalCredit;

    @ManyToOne
    private  InstructorPaymentCredits instructorPaymentCredits;

    @ManyToOne
    private InstructorPayment instructorPayment;

    /**
     * Denotes if a credit entry has been settled
     */
    private boolean isCreditSettled;

    /**
     * Denotes how much is settled for a credit entry
     */
    private Double creditSettledAmount;

    /**
     * Denotes which instructor payment that the debit has settled
     */
    @ManyToOne
    private InstructorPayment settledInstructorPayment;

}

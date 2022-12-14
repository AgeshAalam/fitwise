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
 * Created by Vignesh G on 22/01/21
 */
@Entity
@Getter
@Setter
public class InstructorPaymentCreditHistory extends AuditingEntity {

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

}

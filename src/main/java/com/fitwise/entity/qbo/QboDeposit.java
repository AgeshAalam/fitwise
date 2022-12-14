package com.fitwise.entity.qbo;

import com.fitwise.entity.InstructorPayment;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity for managing qbo payment with fitwise payment
 */
@Entity
@Getter
@Setter
public class QboDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseQboDepositId;

    private String depositId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private QboPayment qboPayment;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InstructorPayment instructorPayment;

    private Boolean needUpdate;

    private String updateStatus;
}

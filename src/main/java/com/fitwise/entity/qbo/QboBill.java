package com.fitwise.entity.qbo;

import com.fitwise.entity.InstructorPayment;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity for managing qbo payment with fitwise payment
 */
@Entity
@Getter
@Setter
public class QboBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseQboBillId;

    private String billId;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private InstructorPayment instructorPayment;

    private Boolean needUpdate;

    private String updateStatus;

    /**
     * Provider charge like App store or Credit card charge
     */
    private Boolean isCCBillCreated;

    private String ccBillId;

    /**
     * Provider Credit card fixed charge
     */
    private Boolean isFixedCCBillCreated;

    private String ccFixedBillId;

    private Boolean billPaid;

    private BigDecimal paidAmount;

}

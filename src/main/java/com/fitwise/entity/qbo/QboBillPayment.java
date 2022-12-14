package com.fitwise.entity.qbo;

import com.fitwise.entity.payments.common.OrderManagement;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity for managing qbo vendor credit with fitwise refund
 */
@Entity
@Getter
@Setter
public class QboBillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseQboBillPaymentId;

    private String qboBillPaymentId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private OrderManagement orderManagement;

    private Boolean needUpdate;

    private String updateStatus;
}

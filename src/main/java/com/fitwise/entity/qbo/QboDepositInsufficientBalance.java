package com.fitwise.entity.qbo;

import com.fitwise.entity.payments.common.OrderManagement;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Entity for managing qbo deposit for insufficient balance deposit
 */
@Entity
@Getter
@Setter
public class QboDepositInsufficientBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseQboDepositId;

    private String depositId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrderManagement orderManagement;

    private Double debitAmount;

    private Boolean needUpdate;

    private String updateStatus;
}

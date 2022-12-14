package com.fitwise.entity.payments.common;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class RefundReasons {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long refundReasonId;

    private String refundReason;

}

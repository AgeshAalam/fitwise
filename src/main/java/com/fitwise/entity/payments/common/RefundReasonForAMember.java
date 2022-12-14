package com.fitwise.entity.payments.common;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class RefundReasonForAMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User member;

    @ManyToOne
    private RefundReasons refundReasons;

    private String refundReasonInWord;

    private String transactionId;

    @ManyToOne
    private OrderManagement orderManagement;

}

package com.fitwise.entity.payments.common;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class InvoiceManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String invoiceNumber;
   
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "order_id")
    private OrderManagement orderManagement;
}

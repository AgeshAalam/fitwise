package com.fitwise.entity.qbo;

import com.fitwise.entity.payments.common.InvoiceManagement;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entity for managing qbo Invoice with fitwise invoice
 */
@Entity
@Getter
@Setter
public class QboInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fitwiseQboInvoiceId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "invoice_id")
    private InvoiceManagement invoice;

    private Boolean needUpdate;

    private String updateStatus;
}

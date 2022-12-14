package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.qbo.QboInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing invoice qbo entity in fitwise
 */
@Repository
public interface QboInvoiceRepository extends JpaRepository<QboInvoice, Long> {

    List<QboInvoice> findByInvoice(final InvoiceManagement invoiceManagement);

    List<QboInvoice> findByNeedUpdate(final Boolean status);
}

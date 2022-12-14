package com.fitwise.repository.order;

import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.payments.common.OrderManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceManagementRepository extends JpaRepository<InvoiceManagement, Long> {

    InvoiceManagement findByOrderManagement(final OrderManagement orderManagement);
    InvoiceManagement findByOrderManagementOrderId( String orderManagement);
}

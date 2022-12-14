package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.qbo.QboVendorCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing vendor credit qbo entity in fitwise
 */
@Repository
public interface QboVendorCreditRepository extends JpaRepository<QboVendorCredit, Long> {

    List<QboVendorCredit> findByOrderManagement(final OrderManagement orderManagement);

    List<QboVendorCredit> findByNeedUpdate(final Boolean status);
}

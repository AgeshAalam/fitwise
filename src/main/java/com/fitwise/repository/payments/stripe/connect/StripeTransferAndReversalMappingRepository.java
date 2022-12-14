package com.fitwise.repository.payments.stripe.connect;

import com.fitwise.entity.payments.stripe.connect.StripeTransferAndReversalMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 05/01/21
 */
@Repository
public interface StripeTransferAndReversalMappingRepository extends JpaRepository<StripeTransferAndReversalMapping, Long> {

    /**
     * @param status
     * @return
     */
    List<StripeTransferAndReversalMapping> findByStatus(String status);

}

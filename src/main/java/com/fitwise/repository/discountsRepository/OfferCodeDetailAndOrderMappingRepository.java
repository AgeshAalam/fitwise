package com.fitwise.repository.discountsRepository;

import com.fitwise.entity.discounts.OfferCodeDetailAndOrderMapping;
import com.fitwise.entity.payments.common.OrderManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 23/11/20
 */
@Repository
public interface OfferCodeDetailAndOrderMappingRepository extends JpaRepository<OfferCodeDetailAndOrderMapping, Long> {

    /**
     * @param order
     * @return
     */
    OfferCodeDetailAndOrderMapping findTop1ByOrderManagementOrderByCreatedDateDesc(OrderManagement order);

}

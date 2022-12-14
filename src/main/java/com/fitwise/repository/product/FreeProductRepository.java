package com.fitwise.repository.product;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.product.FreeProduct;

public interface FreeProductRepository extends JpaRepository<FreeProduct, Long> {

    /**
     * Find by type.
     *
     * @param freeAccessTypeAll the free access type all
     * @return the list
     */
    List<FreeProduct> findByType(final String freeAccessTypeAll);
    
    /**
     * Find by type and free access start date and free access end date.
     *
     * @param freeAccessType the free access type
     * @param startDate the start date
     * @param endDate the end date
     * @return the list
     */
    List<FreeProduct> findByTypeAndFreeAccessStartDateAndFreeAccessEndDate(final String freeAccessType, final Date startDate, final Date endDate);

    /**
     * Find by type and free access start date and free access end date is null.
     *
     * @param freeAccessType the free access type
     * @param startDate the start date
     * @return the list
     */
    List<FreeProduct> findByTypeAndFreeAccessStartDateAndFreeAccessEndDateIsNull(final String freeAccessType, final Date startDate);


}

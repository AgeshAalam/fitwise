package com.fitwise.repository;

import com.fitwise.entity.TaxId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRepository extends JpaRepository<TaxId,Long> {
    TaxId findByUserUserId(final Long userId);
    TaxId findByTaxNumberAndTaxTypesTaxTypeId(final String TaxNumber, final long taxTypeId);

}

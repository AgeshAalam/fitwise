package com.fitwise.repository;

import com.fitwise.entity.TaxTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxTypesRepository extends JpaRepository<TaxTypes,Long> {

    List<TaxTypes> findAll();
    TaxTypes findByTaxTypeId(final Long taxTypeId);




}

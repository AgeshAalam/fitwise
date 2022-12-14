package com.fitwise.repository;

import com.fitwise.entity.ProgramPriceTaxPercentage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramPriceTaxPercentageRepo extends JpaRepository<ProgramPriceTaxPercentage, Long>{

    ProgramPriceTaxPercentage findByPricePercentageId(final Long pricePercentageId);

}

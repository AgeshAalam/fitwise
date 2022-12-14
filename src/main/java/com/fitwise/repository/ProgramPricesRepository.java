package com.fitwise.repository;

import com.fitwise.entity.ProgramPrices;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProgramPricesRepository extends JpaRepository<ProgramPrices,Long> {

	ProgramPrices findByPrice(double price);
	ProgramPrices findByProgramPricesId(Long priceId);
	
	@Query("SELECT p from ProgramPrices p where p.price >= 4.99")
	List<ProgramPrices> findByMinimumPrice();
}

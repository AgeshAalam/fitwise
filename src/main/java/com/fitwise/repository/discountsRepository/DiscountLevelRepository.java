package com.fitwise.repository.discountsRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.discounts.DiscountLevel;

public interface DiscountLevelRepository extends JpaRepository<DiscountLevel, Long>{
	DiscountLevel  findByDiscountLevelName(String discountLevelName);
}

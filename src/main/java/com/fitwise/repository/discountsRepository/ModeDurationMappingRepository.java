package com.fitwise.repository.discountsRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.discounts.ModeDurationMapping;

public interface ModeDurationMappingRepository extends JpaRepository<ModeDurationMapping, Long> {

	List<ModeDurationMapping> findByMode(String mode);
}

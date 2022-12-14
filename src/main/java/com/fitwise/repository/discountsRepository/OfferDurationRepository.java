package com.fitwise.repository.discountsRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.discounts.OfferDuration;

public interface OfferDurationRepository extends JpaRepository<OfferDuration, Long> {
	OfferDuration findByDurationId(int durationId);
}

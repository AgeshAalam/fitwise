package com.fitwise.repository.payments.appleiap;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.payments.appleiap.InitialPricing;


public interface InitialPricingRepository extends JpaRepository<InitialPricing, Long> {

}

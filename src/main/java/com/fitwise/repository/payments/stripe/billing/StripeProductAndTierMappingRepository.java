package com.fitwise.repository.payments.stripe.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.payments.stripe.StripeProductAndTierMapping;

@Repository
public interface StripeProductAndTierMappingRepository extends JpaRepository<StripeProductAndTierMapping, Long>{
	
	StripeProductAndTierMapping findByTierTierId(Long tierId);
	StripeProductAndTierMapping findByTierTierIdAndIsActive(Long tierId, boolean acive);

}

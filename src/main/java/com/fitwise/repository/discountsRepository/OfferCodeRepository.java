package com.fitwise.repository.discountsRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.discounts.OfferCode;

public interface OfferCodeRepository extends JpaRepository<OfferCode, Long> {
	OfferCode findByOfferCodeName(String offerCodeName);	
	OfferCode findTop1ByStatusOrderByCreatedDateAsc(String offerCodeName);
	OfferCode findByOfferCodeNameAndStatus(String offerCodeName,String status);


}

package com.fitwise.repository.payments.appleiap;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.payments.appleiap.IntroOfferUserTracking;

public interface IntroOfferUserTrackingRepository extends JpaRepository<IntroOfferUserTracking, Long>{

	
	IntroOfferUserTracking findTop1ByProgramIdAndUserIdOrderByCreatedDateDesc(Long program,long user);
}

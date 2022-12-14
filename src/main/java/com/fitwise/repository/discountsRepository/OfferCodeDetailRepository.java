package com.fitwise.repository.discountsRepository;

import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import com.fitwise.entity.discounts.OfferCodeDetail;

import java.util.List;

public interface OfferCodeDetailRepository extends JpaRepository<OfferCodeDetail, Long> {
	OfferCodeDetail findByOfferName(String offerName);
	OfferCodeDetail findByOfferNameAndIsInUse(String offerName, boolean isInUse);
	OfferCodeDetail findByOfferCodeAndIsInUse(String offerCode, boolean isInUse);
	OfferCodeDetail findByOfferCodeId(Long offerCodeId);

	OfferCodeDetail findByOfferCodeIdAndOwner(final Long offerCodeId, final User owner);

	List<OfferCodeDetail> findByOwnerAndOfferStatus(final User user, final String status);
	
	List<OfferCodeDetail> findByOfferCodeAndOfferStatusNotInAndIsInUse(String offerCode, List<String> statusList, boolean isInUse);
}

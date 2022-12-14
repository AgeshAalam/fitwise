package com.fitwise.repository.discountsRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.User;
import com.fitwise.entity.discounts.DiscountOfferMapping;

public interface DiscountOfferMappingRepository extends JpaRepository<DiscountOfferMapping, Long> {
	DiscountOfferMapping findByProgramsProgramIdAndOfferCodeDetailOfferCodeId(Long programId,Long offerCodeId);

	/**
	 * Getting active offer codes for programs
	 * @param programId
	 * @param isOfferActive
	 * @return
	 */
	List<DiscountOfferMapping> findByProgramsProgramIdAndOfferCodeDetailIsInUse(Long programId, boolean isOfferActive);

	/**
	 * @param programId
	 * @param isOfferActive
	 * @param offerStatus
	 * @return
	 */
	List<DiscountOfferMapping> findByProgramsProgramIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(Long programId, boolean isOfferActive, String offerStatus);

	DiscountOfferMapping findByOfferCodeDetailOfferCodeId(Long offerCodeId);
	
	List<DiscountOfferMapping> findByProgramsProgramIdAndNeedMailUpdate(Long programId, Boolean isMailRequired);
	
	List<DiscountOfferMapping> findByProgramsProgramIdAndOfferCodeDetailIsNewUser(Long programId, Boolean isNewUser);

	List<DiscountOfferMapping> findByProgramsProgramIdAndNeedDiscountUpdateAndOfferCodeDetailIsNewUser(Long programId,Boolean needDiscountUpd, Boolean isNewUser);
	
	List<DiscountOfferMapping> findByProgramsProgramIdAndOfferCodeDetailIsNewUserAndOfferCodeDetailOfferModeAndOfferCodeDetailOfferStatus(Long programId,boolean isNewUser, String mode,String offerStatus);

	List<DiscountOfferMapping> findByProgramsProgramIdAndOfferCodeDetailIsNewUserAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(Long programId, boolean isNewUser, boolean isInUse, String offerStatus);

	DiscountOfferMapping findTop1ByProgramsProgramIdAndOfferCodeDetailIsNewUserAndOfferCodeDetailOfferStatus(
			Long programId, boolean b, String offerActive);

	DiscountOfferMapping findByProgramsProgramIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferNameAndOfferCodeDetailOwner(
			Long programId, boolean b, String offerReferenceName, User user);


}

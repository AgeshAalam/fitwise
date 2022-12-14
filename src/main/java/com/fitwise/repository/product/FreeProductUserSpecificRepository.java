package com.fitwise.repository.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.User;
import com.fitwise.entity.product.FreeAccessPackages;
import com.fitwise.entity.product.FreeAccessProgram;
import com.fitwise.entity.product.FreeProductUserSpecific;

@Repository
public interface FreeProductUserSpecificRepository
		extends JpaRepository<FreeProductUserSpecific, Long>, JpaSpecificationExecutor<FreeProductUserSpecific> {

	/**
	 * Find by user and free access program.
	 *
	 * @param user the user
	 * @param freeAccessProgram the free access program
	 * @return the list
	 */
	List<FreeProductUserSpecific> findByUserAndFreeAccessProgram(User user, FreeAccessProgram freeAccessProgram);

	/**
	 * Find by user and free access subscription packages.
	 *
	 * @param user the user
	 * @param freeAccessPackages the free access packages
	 * @return the list
	 */
	List<FreeProductUserSpecific> findByUserAndFreeAccessSubscriptionPackages(User user,
			FreeAccessPackages freeAccessPackages);
	
	/**
	 * Find by free product user specific id.
	 *
	 * @param freeProductUserSpecificId the free product user specific id
	 * @return the free product user specific
	 */
	FreeProductUserSpecific findByFreeProductUserSpecificId(final Long freeProductUserSpecificId);
}

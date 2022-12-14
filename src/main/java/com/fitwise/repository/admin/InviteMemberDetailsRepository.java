package com.fitwise.repository.admin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.admin.InviteMemberDetails;

@Repository
public interface InviteMemberDetailsRepository extends JpaRepository<InviteMemberDetails, Long>, JpaSpecificationExecutor<InviteMemberDetails> {
	
	/**
	 * Find by email.
	 *
	 * @param email the email
	 * @return the list
	 */
	List<InviteMemberDetails> findByEmail(String email);
	
	/**
	 * Find by user registered.
	 *
	 * @param userIsRegistered the user is registered
	 * @return the list
	 */
	List<InviteMemberDetails> findByUserRegistered(boolean userIsRegistered);
}

package com.fitwise.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.User;
import com.fitwise.entity.UserRole;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * The Interface UserRepository.
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
	 
 	/**
 	 * Find by email.
 	 *
 	 * @param email the email
 	 * @return the user
 	 */
 	User findByEmail(final String email);
	 
 	/**
 	 * Find by user id.
 	 *
 	 * @param userId the user id
 	 * @return the user
 	 */
 	User findByUserId(final long userId);

	/**
	 * User list by Id list
	 * @param userIdList
	 * @return
	 */
 	List<User> findByUserIdIn(List<Long> userIdList);

    /**
     * Exists by user id.
     *
     * @param userId the user id
     * @return true, if successful
     */
    boolean existsByUserId(long userId);

	/**
	 * Find by role.
	 *
	 * @param userRole the user role
	 * @param pageable the pageable
	 * @return the page
	 */
	Page<User> findByUserRoleMappingsUserRole(UserRole userRole, Pageable pageable);

	/**
	 * Find by role.
	 *
	 * @param userRole the user role
	 * @return the list of users
	 */
	List<User> findByUserRoleMappingsUserRole(UserRole userRole);

	List<User> findByCreatedDateGreaterThanAndCreatedDateLessThan(LocalDate start, LocalDate end);


	/**
	 * Get total count of instructors/members before a particular date
	 * @param userRole
	 * @param endDate
	 * @return
	 */
	int countByUserRoleMappingsUserRoleAndCreatedDateLessThan(UserRole userRole, Date endDate);

	/**
	 * Get total count of instructors/members
	 * @param roleName
	 * @return
	 */
	Long countByUserRoleMappingsUserRoleName(String roleName);
}

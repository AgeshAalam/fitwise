package com.fitwise.repository;

import java.util.List;

import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.UserProgramGoalsMapping;

/**
 * The Interface UserProgramGoalsMappingRepository.
 */
@Repository
public interface UserProgramGoalsMappingRepository extends JpaRepository<UserProgramGoalsMapping, Long> {

	/**
	 * Find by user user id.
	 *
	 * @param userId the user id
	 * @return the list
	 */
	List<UserProgramGoalsMapping> findByUserUserId(long userId);

	/**
	 * Exists by user user id.
	 *
	 * @param userId the user id
	 * @return true, if successful
	 */
	boolean existsByUserUserId(long userId);

	/**
	 * Find by user.
	 *
	 * @param user the user
	 * @return the list
	 */
	 List<UserProgramGoalsMapping> findByUser(User user);

}

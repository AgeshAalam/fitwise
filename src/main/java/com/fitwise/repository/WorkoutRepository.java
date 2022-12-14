package com.fitwise.repository;

import com.fitwise.entity.User;
import com.fitwise.entity.Workouts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workouts, Long>{
	
	List<Workouts> findByWorkoutIdAndOwnerUserId(final Long workoutId, final Long userId);

	List<Workouts> findByOwnerUserId(final Long userId);

	/**
	 * @param userId
	 * @param pageable
	 * @return
	 */
	Page<Workouts> findByOwnerUserId(Long userId, Pageable pageable);

	Workouts findByWorkoutId(Long workoutId);

	List<Workouts> findByWorkoutIdAndOwner(final Long workoutId, final User user);

	List<Workouts> findByOwner(final User user);
	
	List<Workouts> findByTitleIgnoreCaseContaining(final String title);

	List<Workouts> findByImageImageId(final Long imageId);

	List<Workouts> findByOwnerUserIdAndTitleIgnoreCaseContainingOrOwnerUserIdAndDescriptionIgnoreCaseContaining(Long userId, String s, Long userId1, String s1);

	/**
	 * @param userId
	 * @param titleSearch
	 * @param pageable
	 * @return
	 */
	Page<Workouts> findByOwnerUserIdAndTitleIgnoreCaseContaining(Long userId, String titleSearch, Pageable pageable);

	/**
	 * Find Workout by owner and title
	 *
	 * @param userId
	 * @param title
	 * @return Workout by owner and title
	 */
	Workouts findByOwnerUserIdAndTitleIgnoreCase(long userId, String title);

	int countByOwnerUserId(Long userId);

	boolean existsByWorkoutId(Long workoutId);
}
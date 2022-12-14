package com.fitwise.repository;

import org.apache.xpath.operations.Bool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.Exercises;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Interface ExerciseRepository.
 */
@Repository
public interface ExerciseRepository extends JpaRepository<Exercises, Long>, JpaSpecificationExecutor<Exercises> {

    /**
     * Find by exercise id and owner user id.
     *
     * @param exerciseId the exercise id
     * @param userId the user id
     * @return the list
     */
    Exercises findByExerciseIdAndOwnerUserId(Long exerciseId, Long userId);

    /**
     * Find by owner user id.
     *
     * @param userId the user id
     * @return the list
     */
    List<Exercises> findByOwnerUserId(Long userId);

    /**
     * @param userId
     * @param pageable
     * @return
     */
    Page<Exercises> findByOwnerUserId(Long userId, Pageable pageable);

    /**
     * Find by exercise id.
     *
     * @param exerciseLong the exercise long
     * @return the exercises
     */
    Exercises findByExerciseId(Long exerciseLong);
	
    List<Exercises> findByTitleIgnoreCaseContaining(String exercise);

    List<Exercises> findByOwnerUserIdAndTitleIgnoreCaseContainingOrOwnerUserIdAndDescriptionIgnoreCaseContaining(long userId, String title, long userId2, String description);

    /**
     * @param userId
     * @param title
     * @param pageable
     * @return
     */
    Page<Exercises> findByOwnerUserIdAndTitleIgnoreCaseContaining(long userId, String title, Pageable pageable);

    /**
     * Find Exercise by owner and title
     *
     * @param userId
     * @param title
     * @return Exercise by owner and title
     */
    List<Exercises> findByOwnerUserIdAndTitleIgnoreCase(long userId, String title);

    int countByOwnerUserId(Long userId);

    /**
     * Get count of stock exercises
     * @param isByAdmin
     * @return
     */
    long countByIsByAdminAndOwnerNotNull(final Boolean isByAdmin);

    /**
     * find by exercise id and admin user
     * @param exerciseId
     * @param isByAdmin
     * @return
     */
    Exercises findByExerciseIdAndIsByAdmin(Long exerciseId, Boolean isByAdmin);

    /**
     * Find exercise by admin user and title
     * @param isByAdmin
     * @param title
     * @return
     */
    List<Exercises> findByIsByAdminAndTitleIgnoreCase(Boolean isByAdmin, String title);

    List<Exercises> findByEquipmentsEquipmentId(Long equipmentId);


}
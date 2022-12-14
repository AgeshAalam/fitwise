package com.fitwise.repository;

import com.fitwise.entity.FlaggedExercisesSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 25/03/20
 */

@Repository
public interface FlaggedExercisesSummaryRepository extends JpaRepository<FlaggedExercisesSummary, Long> {

    /**
     * @param title
     * @return
     */
    Page<FlaggedExercisesSummary> findByExerciseTitleIgnoreCaseContainingAndFlagStatusIn(String title, List<String> flagStatusList, Pageable pageable);

    /**
     * @param exerciseId
     * @return
     */
    FlaggedExercisesSummary findByExerciseExerciseId(long exerciseId);

    /**
     * @param exerciseIdList
     * @param flagStatus
     * @return
     */
    boolean existsByExerciseExerciseIdInAndFlagStatus(List<Long> exerciseIdList, String flagStatus);

    /**
     * @param exerciseId
     * @param flagStatus
     * @return
     */
    boolean existsByExerciseExerciseIdAndFlagStatus(Long exerciseId, String flagStatus);

    /**
     * @param flagStatusList
     * @param pageable
     * @return
     */
    Page<FlaggedExercisesSummary> findByFlagStatusIn(List<String> flagStatusList, Pageable pageable);
}

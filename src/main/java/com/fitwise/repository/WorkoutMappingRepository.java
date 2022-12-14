package com.fitwise.repository;

import com.fitwise.entity.Programs;
import com.fitwise.entity.WorkoutMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutMappingRepository extends JpaRepository<WorkoutMapping,Long> {
    WorkoutMapping findByProgramsProgramIdAndWorkoutWorkoutId(final Long programId,final Long workoutId);

    List<WorkoutMapping> findByPrograms(Programs program);

    /**
     * Get workout mappings for the given workout id and in a list of given program status
     *
     * @param workoutId
     * @param statusList
     * @return workout mappings list for the given workout id and in a list of given program status
     */
    List<WorkoutMapping> findByWorkoutWorkoutIdAndProgramsStatusIn(long workoutId, List<String> statusList);

    int countByWorkoutWorkoutIdAndProgramsStatusIn(long workoutId, List<String> statusList);

    /**
     * Get workout mappings in given workout id list
     *
     * @param workoutIdList
     * @return
     */
    List<WorkoutMapping> findByWorkoutWorkoutIdIn(List<Long> workoutIdList);

    List<WorkoutMapping> findByWorkoutWorkoutId(Long workoutId);

    List<WorkoutMapping> findByWorkoutWorkoutIdAndProgramsProgramIdIn(final long workoutId, final List<Long> programIds);

    /**
     * List of WorkoutMapping based on workout list and program status list
     * @param workoutIdList
     * @param statusList
     * @return
     */
    List<WorkoutMapping> findByWorkoutWorkoutIdInAndProgramsStatusIn(List<Long> workoutIdList, List<String> statusList);
}

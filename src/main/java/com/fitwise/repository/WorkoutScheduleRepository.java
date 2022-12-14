package com.fitwise.repository;

import com.fitwise.entity.Programs;
import com.fitwise.entity.WorkoutSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface WorkoutScheduleRepository extends JpaRepository<WorkoutSchedule,Long> {

    WorkoutSchedule findByWorkoutScheduleId(final Long workoutScheduleId);

    WorkoutSchedule findByOrderAndProgramsProgramId(final Long order,final Long programId);

    @Modifying
    @Transactional
    @Query(value="delete from WorkoutSchedule ws where ws.programs = ?1")
    void deleteByPrograms(final Programs programs);

    /**
     * Get WorkoutSchedule by Id and workoutId
     * @param workoutScheduleId
     * @param workoutId
     * @return
     */
    WorkoutSchedule findByWorkoutScheduleIdAndWorkoutWorkoutId(long workoutScheduleId, long workoutId);
}

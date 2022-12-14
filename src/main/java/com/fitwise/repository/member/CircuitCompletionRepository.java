package com.fitwise.repository.member;

import com.fitwise.entity.CircuitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/*
 * Created by Vignesh G on 16/05/20
 */
@Repository
public interface CircuitCompletionRepository extends JpaRepository<CircuitCompletion, Long> {

    /**
     * Get CircuitCompletion row for user based on programId, workoutScheduleId, circuitScheduleId
     * @param userId
     * @param programId
     * @param workoutScheduleId
     * @param circuitScheduleId
     * @return
     */
    CircuitCompletion findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdOrderByCompletedDateDesc(Long userId, Long programId, Long workoutScheduleId, Long circuitScheduleId);

    /**
     * @param circuitScheduleId
     * @return
     */
    List<CircuitCompletion> findByCircuitScheduleId(Long circuitScheduleId);

    /**
     * List of circuits completed under by an user in a program
     * @param userId
     * @param programId
     * @return
     */
    List<CircuitCompletion> findByMemberUserIdAndProgramProgramId(Long userId, Long programId);

    /**
     * @param userId
     * @return
     */
    List<CircuitCompletion> findByMemberUserId(Long userId);


    List<CircuitCompletion> findByWorkoutScheduleIdIn(List<Long> workoutScheduleIdList);

    /**
     * @param circuitScheduleIdList
     */
    List<CircuitCompletion> findByCircuitScheduleIdIn(List<Long> circuitScheduleIdList);

    List<CircuitCompletion> findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleId(Long userId, Long programId, Long workoutScheduleId);


}

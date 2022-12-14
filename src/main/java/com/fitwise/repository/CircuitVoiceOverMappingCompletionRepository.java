package com.fitwise.repository;

import com.fitwise.entity.CircuitVoiceOverMappingCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CircuitVoiceOverMappingCompletionRepository extends JpaRepository<CircuitVoiceOverMappingCompletion,Long> {

    /**
     * @param memberId
     * @param programId
     * @param workoutScheduleId
     * @param circuitScheduleId
     * @param circuitAndVoiceOverMappingId
     * @return
     */
    CircuitVoiceOverMappingCompletion findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdAndCircuitVoiceOverMappingIdOrderByCompletedDateDesc(Long memberId, Long programId, Long workoutScheduleId, Long circuitScheduleId, Long circuitAndVoiceOverMappingId);

    /**
     * @param userId
     * @param programId
     * @param workoutScheduleId
     * @param circuitScheduleId
     * @return
     */
    List<CircuitVoiceOverMappingCompletion> findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleId(Long userId, Long programId, Long workoutScheduleId, Long circuitScheduleId);



}

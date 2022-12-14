package com.fitwise.repository.member;

import com.fitwise.entity.CircuitCompletionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 01/07/20
 */
@Repository
public interface CircuitCompletionAuditRepository extends JpaRepository<CircuitCompletionAudit, Long> {

    /**
     * @param workoutScheduleIdList
     */
    void deleteByWorkoutScheduleIdIn(List<Long> workoutScheduleIdList);

    List<CircuitCompletionAudit> findByWorkoutScheduleIdIn(List<Long> workoutScheduleIdList);

    /**
     * @param circuitScheduleIdList
     */
    List<CircuitCompletionAudit> findByCircuitScheduleIdIn(List<Long> circuitScheduleIdList);

}

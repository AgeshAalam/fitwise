package com.fitwise.repository.circuit;

import com.fitwise.entity.CircuitSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * Created by Vignesh G on 11/05/20
 */
@Repository
public interface CircuitScheduleRepository extends JpaRepository<CircuitSchedule, Long> {

    /**
     * Get list of CircuitSchedule in a circuit
     * @param circuitId
     * @return
     */
    List<CircuitSchedule> findByCircuitCircuitId(long circuitId);

    List<CircuitSchedule> findByWorkoutWorkoutId(long WorkoutId);

    /**
     * Circuit schedules with a list of circuitId
     * @param circuitIdList
     * @return
     */
    List<CircuitSchedule> findByCircuitCircuitIdIn(List<Long> circuitIdList);

    /**
     * Find workout's circuit schedule by circuit order
     * @param order
     * @param workoutId
     * @return
     */
    CircuitSchedule findByOrderAndWorkoutWorkoutId(Long order, Long workoutId);

    /**
     * @param circuitScheduleId
     */
    @Modifying
    @Transactional
    @Query(value="delete from CircuitSchedule cs where cs.circuitScheduleId = ?1")
    void deleteByCircuitScheduleId(long circuitScheduleId);

}

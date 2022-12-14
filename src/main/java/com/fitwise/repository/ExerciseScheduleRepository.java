package com.fitwise.repository;

import com.fitwise.entity.Circuit;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.WorkoutRestVideos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ExerciseScheduleRepository extends JpaRepository<ExerciseSchedulers, Long> {

    ExerciseSchedulers findByExerciseScheduleId(final Long exerciseScheduleId);

    void deleteByExercise(Exercises exercises);

    @Modifying
    @Transactional
    @Query(value="delete from ExerciseSchedulers es where es.exerciseScheduleId = ?1")
    void deleteByExerciseScheduleId(long exerciseScheduleId);

    /**
     * Get list of Exerciseschedulers from a given exercise Id
     *
     * @param exerciseId
     * @return
     */
    List<ExerciseSchedulers> findByExerciseExerciseId(long exerciseId);

    /**
     * Non rest Exercise schedule count based circuit
     * @param circuit
     * @param workoutRestVideos
     * @return
     */
    int countByCircuitAndWorkoutRestVideo(Circuit circuit, WorkoutRestVideos workoutRestVideos);

    /**
     * Get list of ExerciseSchedulers of a circuit
     * @param circuitId
     * @return
     */
    List<ExerciseSchedulers> findByCircuitCircuitId(long circuitId);

    List<ExerciseSchedulers> findByVoiceOverVoiceOverId(long voiceOverId);

}

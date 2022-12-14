package com.fitwise.repository;

import com.fitwise.entity.ProgramWorkoutMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ProgramWorkoutMappingRepository extends JpaRepository<ProgramWorkoutMapping, Long> {

    List<ProgramWorkoutMapping> findByProgramProgramId(final Long programId);

    ProgramWorkoutMapping findByProgramProgramIdAndWorkoutWorkoutId(Long programId, Long workoutId);
}

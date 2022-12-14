package com.fitwise.repository;

import com.fitwise.entity.WorkoutRestVideos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRestRepository extends JpaRepository<WorkoutRestVideos,Long> {

    WorkoutRestVideos findByWorkoutRestVideoId(final Long workoutRestVideoId);

}

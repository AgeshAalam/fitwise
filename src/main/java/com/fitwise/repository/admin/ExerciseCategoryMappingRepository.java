package com.fitwise.repository.admin;

import com.fitwise.entity.Exercises;
import com.fitwise.entity.admin.ExerciseCategory;
import com.fitwise.entity.admin.ExerciseCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseCategoryMappingRepository extends JpaRepository<ExerciseCategoryMapping, Long> {

    long countByExerciseCategoryCategoryId(Long exerciseCategoryId);

    List<ExerciseCategoryMapping> findByExercise(Exercises exercise);

    ExerciseCategoryMapping findByExerciseAndExerciseCategory(Exercises exercises, ExerciseCategory exerciseCategory);

}

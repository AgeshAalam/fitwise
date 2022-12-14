package com.fitwise.repository.admin;

import com.fitwise.entity.admin.ExerciseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseCategoryRepository extends JpaRepository<ExerciseCategory, Long> {

    ExerciseCategory findByCategoryId(long categoryId);
}

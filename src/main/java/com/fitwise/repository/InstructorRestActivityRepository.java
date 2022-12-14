package com.fitwise.repository;

import com.fitwise.entity.InstructorRestActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 18/05/20
 */
@Repository
public interface InstructorRestActivityRepository extends JpaRepository<InstructorRestActivity, Long> {
}

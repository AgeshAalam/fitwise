package com.fitwise.repository;

import com.fitwise.entity.FeaturedProgramTypeAndPrograms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeaturedProgramTypeAndProgramsRepository extends JpaRepository<FeaturedProgramTypeAndPrograms, Long> {

    List<FeaturedProgramTypeAndPrograms> findByProgramStatusOrderByFeaturedProgramId(final String status);

}

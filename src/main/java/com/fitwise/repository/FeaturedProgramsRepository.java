package com.fitwise.repository;

import com.fitwise.entity.FeaturedPrograms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeaturedProgramsRepository extends JpaRepository<FeaturedPrograms,Long> {

    List<FeaturedPrograms> findAllByOrderById();

    List<FeaturedPrograms> findByProgramStatusOrderById(final String status);
}
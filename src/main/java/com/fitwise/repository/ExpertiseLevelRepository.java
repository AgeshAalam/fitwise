package com.fitwise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.ExpertiseLevels;

@Repository
public interface ExpertiseLevelRepository extends JpaRepository<ExpertiseLevels, Long> {
	List<ExpertiseLevels> findAll();
	ExpertiseLevels findByExpertiseLevel(String level);
	ExpertiseLevels findByExpertiseLevelId(Long expertiseLevelId);
}

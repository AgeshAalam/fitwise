package com.fitwise.repository;

import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.YearsOfExpertise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YearsOfExpertiseRepository extends JpaRepository<YearsOfExpertise,Long> {
    List<YearsOfExpertise> findAll();
    YearsOfExpertise findByExperienceId(final Long experienceId);

    YearsOfExpertise findByNumberOfYears(final Long numberOfYears);




}

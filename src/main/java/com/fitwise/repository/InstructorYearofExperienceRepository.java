package com.fitwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.InstructorProgramExperience;
import com.fitwise.entity.YearsOfExpertise;

/**
 * The Interface InstructorYearofExperienceRepository.
 */
public interface InstructorYearofExperienceRepository extends JpaRepository<YearsOfExpertise,Long>{

	/**
	 * Find by experience id.
	 *
	 * @param yearOfExperience the year of experience
	 * @return the instructor program experience
	 */
	InstructorProgramExperience findByExperienceId(final Long yearOfExperience);
}

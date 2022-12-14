package com.fitwise.repository;

import java.util.List;

import com.fitwise.entity.ProgramExpertiseMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.ProgramExpertiseGoalsMapping;

@Repository
public interface ProgramExpertiseGoalsMappingRepository extends JpaRepository<ProgramExpertiseGoalsMapping, Long> {

	public List<ProgramExpertiseGoalsMapping> findByProgramExpertiseMappingProgramExpertiseMappingId(Long programExpertiseMappingId);

	public ProgramExpertiseGoalsMapping findByProgramExpertiseGoalsMappingId(Long programGoalsMappingId);

	/**
	 * @param programGoalsMappingId
	 * @return
	 */
	List<ProgramExpertiseGoalsMapping> findByProgramExpertiseGoalsMappingIdIn(List<Long> programGoalsMappingId);

	/**
	 * @param programGoalsMappingId
	 * @return
	 */
	long countByProgramExpertiseGoalsMappingIdIn(List<Long> programGoalsMappingId);

	//public ProgramExpertiseGoalsMapping findByProgramExpertiseGoalsMapping(ProgramExpertiseGoalsMapping programExpertiseGoalsMapping);


	public boolean existsByProgramExpertiseGoalsMappingId(long mappingId);

	List<ProgramExpertiseGoalsMapping> findByProgramExpertiseMappingProgramTypeProgramTypeId(Long programTypeId);





}

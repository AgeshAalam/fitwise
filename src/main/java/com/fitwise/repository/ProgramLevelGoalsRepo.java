package com.fitwise.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.entity.ProgramGoals;
import com.fitwise.entity.ProgramTypeLevelGoalMapping;
import com.fitwise.entity.ProgramTypes;

public interface ProgramLevelGoalsRepo extends JpaRepository<ProgramTypeLevelGoalMapping, Integer> {
	public List<ProgramTypeLevelGoalMapping> findByProgramTypeAndExpertiseLevel(Optional<ProgramTypes> programTypeObj,
			Optional<ExpertiseLevels> expLevelObj);

	public ProgramTypeLevelGoalMapping findByProgramGoal(ProgramGoals programGoal);
	
	public ProgramTypeLevelGoalMapping findByProgramType(ProgramTypes programType);
}

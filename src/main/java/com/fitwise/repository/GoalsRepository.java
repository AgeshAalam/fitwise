package com.fitwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.ProgramGoals;

import java.util.List;

@Repository
public interface GoalsRepository extends JpaRepository<ProgramGoals, Integer>{

		ProgramGoals findByProgramGoalId(final Long programGoalId);

		List<ProgramGoals> findAll();

	    // ProgramGoals findByProgramGoal(ProgramGoals programGoals);


}
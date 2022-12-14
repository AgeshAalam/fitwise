package com.fitwise.repository;

import com.fitwise.entity.ProgramWiseGoal;
import com.fitwise.entity.Programs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramWiseGoalRepository extends JpaRepository<ProgramWiseGoal, Long> {
    void deleteByProgram(final Programs program);

    List<ProgramWiseGoal> findByProgramExpertiseGoalsMappingProgramGoalsProgramGoalIgnoreCaseContainingAndProgramOwnerUserIdAndProgramStatus(String searchName, long id, String status);

    List<ProgramWiseGoal> findByProgramExpertiseGoalsMappingProgramGoalsProgramGoalIgnoreCaseContainingAndProgramOwnerUserId(String searchName, long id);

    List<ProgramWiseGoal> findByProgramExpertiseGoalsMappingProgramGoalsProgramGoalIgnoreCaseContainingAndProgramStatus(String searchName1, String status);
}

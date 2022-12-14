package com.fitwise.repository;

import com.fitwise.entity.ProgramExpertiseMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramExpertiseMappingRepo extends JpaRepository<ProgramExpertiseMapping, Integer> {

    public List<ProgramExpertiseMapping> findByProgramTypeProgramTypeId(int programTypeId);
}

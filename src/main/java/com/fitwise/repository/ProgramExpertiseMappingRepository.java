package com.fitwise.repository;

import com.fitwise.entity.ProgramExpertiseMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramExpertiseMappingRepository extends JpaRepository<ProgramExpertiseMapping, Long> {

    public List<ProgramExpertiseMapping> findByProgramTypeProgramTypeId(long programTypeId);

    public List<ProgramExpertiseMapping> findByProgramTypeProgramTypeIdAndExpertiseLevelExpertiseLevelId(Long programTypeId , Long expertiseLevelId );

    List<ProgramExpertiseMapping> findAll();

    ProgramExpertiseMapping findByProgramExpertiseMappingId(final Long programExpertiseMappingId);
}

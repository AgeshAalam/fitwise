package com.fitwise.repository;

import com.fitwise.entity.InstructorProgramExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InstructorExperienceRepository extends JpaRepository<InstructorProgramExperience, Long>, JpaSpecificationExecutor<InstructorProgramExperience> {

    /**
     * InstructorProgramExperience list based on programTypeIds
     * @param programTypeIds
     * @return
     */
    List<InstructorProgramExperience> findByProgramTypeProgramTypeIdIn(List<Long> programTypeIds);

    InstructorProgramExperience findByProgramTypeProgramTypeIdAndUserUserId(final Long programTypeId,final Long userId);

    List<InstructorProgramExperience> findByUserUserId(final long userId);


}

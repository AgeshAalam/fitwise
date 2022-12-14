package com.fitwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.ProgramTypes;

import java.util.List;

/**
 * The Interface ProgramTypeRepository.
 */
public interface ProgramTypeRepository extends JpaRepository<ProgramTypes, Long> {

    List<ProgramTypes> findByOrderByProgramTypeNameAsc();

    List<ProgramTypes> findAll();

    /**
     * Find by program type name.
     *
     * @param programType the type
     * @return the program types
     */
    ProgramTypes findByProgramTypeName(String programType);
    
    /**
     * Find by program type id.
     *
     * @param programTypeId the program type id
     * @return the program types
     */
    ProgramTypes findByProgramTypeId(final Long programTypeId);
    
    /**
     * Find by program type name ignore case containing.
     *
     * @param programType the program type
     * @return the program types
     */
    ProgramTypes findByProgramTypeNameIgnoreCaseContaining(List<String> programType);
}

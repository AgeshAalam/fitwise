package com.fitwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.ProgramSubTypes;

public interface ProgramSubTypeRepository extends JpaRepository<ProgramSubTypes, Long> {

	ProgramSubTypes findByProgramTypeProgramTypeId(long programTypeId);

}

package com.fitwise.repository;

import com.fitwise.entity.SamplePrograms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleProgramsRepository extends JpaRepository<SamplePrograms, Long> {

    /**
     * To check if a program Id belongs to a sample program
     * @param programId
     * @return
     */
    boolean existsByProgramsProgramId(Long programId);

    /**
     * @param status
     * @return
     */
    List<SamplePrograms> findByProgramsStatusIn(List<String> status);

}

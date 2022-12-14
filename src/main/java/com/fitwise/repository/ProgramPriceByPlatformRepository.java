package com.fitwise.repository;

import com.fitwise.entity.ProgramPriceByPlatform;
import com.fitwise.entity.Programs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProgramPriceByPlatformRepository extends JpaRepository<ProgramPriceByPlatform, Long> {
    Set<ProgramPriceByPlatform> findByProgram(Programs programs);

    ProgramPriceByPlatform findByProgramPriceByPlatformId(Long programPriceByPlatformId);

    void deleteByProgram(Programs program);
}

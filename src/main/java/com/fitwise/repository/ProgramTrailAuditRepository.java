package com.fitwise.repository;

import com.fitwise.entity.ProgramTrailAudit;
import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramTrailAuditRepository  extends JpaRepository<ProgramTrailAudit, Long> {

    @Query(value = "Select pta from ProgramTrailAudit pta where pta.user IN :users and program.programId = :programId group by user")
    List<ProgramTrailAudit> findUniqueProgramTrails(@Param("users") List<User> users , @Param("programId") Long programId);
}

package com.fitwise.repository;

import com.fitwise.entity.ProgramViewsAudit;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProgramViewsAuditRepository  extends JpaRepository<ProgramViewsAudit , Long>, JpaSpecificationExecutor<ProgramViewsAudit> {
    List<ProgramViewsAudit> findDistinctUserByProgram(Programs program);

    int countByProgram(Programs program);

    List<ProgramViewsAudit> findByProgramOrderByUser(Programs program);

    @Query(value = "Select pva from ProgramViewsAudit pva where program.programId =:programId group by user")
    List<ProgramViewsAudit> findProgramViewsUnique(@Param("programId") Long programId);

    List<ProgramViewsAudit> findByUserNotNullAndDateBetween( Date startDate, Date enDdate);

}

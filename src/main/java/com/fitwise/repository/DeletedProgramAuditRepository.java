package com.fitwise.repository;

import com.fitwise.entity.DeletedProgramAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedProgramAuditRepository extends JpaRepository<DeletedProgramAudit, Long> {

    boolean existsByProgramProgramId(Long programId);
}

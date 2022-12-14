package com.fitwise.repository;

import com.fitwise.entity.BlockedProgramsAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedProgramsAuditRepository extends JpaRepository<BlockedProgramsAudit, Long> {
}

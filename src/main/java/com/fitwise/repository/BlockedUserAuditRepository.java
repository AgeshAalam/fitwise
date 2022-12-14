package com.fitwise.repository;

import com.fitwise.entity.BlockedUserAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedUserAuditRepository extends JpaRepository<BlockedUserAudit, Long> {
}

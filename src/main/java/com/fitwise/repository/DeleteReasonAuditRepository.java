package com.fitwise.repository;

import com.fitwise.entity.DeleteReasonAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeleteReasonAuditRepository extends JpaRepository<DeleteReasonAudit, Long> {
}

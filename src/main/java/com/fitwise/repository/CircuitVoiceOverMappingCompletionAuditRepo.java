package com.fitwise.repository;

import com.fitwise.entity.CircuitVoiceOverMappingCompletionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CircuitVoiceOverMappingCompletionAuditRepo extends JpaRepository<CircuitVoiceOverMappingCompletionAudit,Long> {

}

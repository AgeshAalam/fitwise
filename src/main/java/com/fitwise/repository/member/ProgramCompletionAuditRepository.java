package com.fitwise.repository.member;

import com.fitwise.entity.member.completion.ProgramCompletionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 02/03/21
 */
@Repository
public interface ProgramCompletionAuditRepository extends JpaRepository<ProgramCompletionAudit, Long> {
}

package com.fitwise.repository;

import com.fitwise.entity.DeletedUserAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeletedUserAuditRepository extends JpaRepository<DeletedUserAudit, Long > {

    boolean existsByUserUserIdAndUserRoleNameIgnoreCaseContaining(Long userId, String role);

}

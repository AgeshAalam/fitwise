package com.fitwise.repository.view;

import com.fitwise.entity.view.ViewAdminPrograms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ViewAdminProgramsRepository extends JpaRepository<ViewAdminPrograms, Long>, JpaSpecificationExecutor<ViewAdminPrograms> {
}

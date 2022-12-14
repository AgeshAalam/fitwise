package com.fitwise.repository.view;

import com.fitwise.entity.view.ViewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ViewMemberRepository extends JpaRepository<ViewMember, Long>, JpaSpecificationExecutor<ViewMember> {
}

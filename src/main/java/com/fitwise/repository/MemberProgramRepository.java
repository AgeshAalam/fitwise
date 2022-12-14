package com.fitwise.repository;

import com.fitwise.entity.MemberPrograms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberProgramRepository extends JpaRepository<MemberPrograms,Long> {

    List<MemberPrograms> findByUserUserId(final Long userId);

    MemberPrograms findByUserUserIdAndProgramsProgramId(Long userId, Long programId);
}

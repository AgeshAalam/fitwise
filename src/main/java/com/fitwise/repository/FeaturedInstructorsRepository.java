
package com.fitwise.repository;

import com.fitwise.entity.FeaturedInstructors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface FeaturedInstructorsRepository extends JpaRepository<FeaturedInstructors,Long>, JpaSpecificationExecutor<FeaturedInstructors> {

    List<FeaturedInstructors> findAllByOrderById();

    Page<FeaturedInstructors> findByUserUserIdNotIn(final List<Long> blockedUserIds, Pageable pageable);

    Long countByUserUserIdNotIn(final List<Long> blockedUserIds);
}
package com.fitwise.repository;

import com.fitwise.entity.DeleteReasons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeleteReasonsRepository extends JpaRepository<DeleteReasons , Long> {


    boolean existsByDeleteReasonId(Long delteResonsId);
}

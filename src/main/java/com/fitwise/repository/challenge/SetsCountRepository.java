package com.fitwise.repository.challenge;

import com.fitwise.entity.SetsCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetsCountRepository extends JpaRepository<SetsCount, Long> {
}

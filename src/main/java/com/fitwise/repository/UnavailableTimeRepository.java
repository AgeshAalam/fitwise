package com.fitwise.repository;

import com.fitwise.entity.UnavailableTimes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnavailableTimeRepository extends JpaRepository<UnavailableTimes,Long> {
}

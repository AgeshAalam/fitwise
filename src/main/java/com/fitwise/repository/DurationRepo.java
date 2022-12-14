package com.fitwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.Duration;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DurationRepo extends JpaRepository<Duration, Long>{

	List<Duration> findAllByOrderByDurationAsc();

	Duration findByDuration(Long duration);

	Duration findByDurationId(Long durationId);

}

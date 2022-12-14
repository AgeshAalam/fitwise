package com.fitwise.repository;

import com.fitwise.entity.TimeSpan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 21/09/20
 */
@Repository
public interface TimeSpanRepository extends JpaRepository<TimeSpan, Long> {
}

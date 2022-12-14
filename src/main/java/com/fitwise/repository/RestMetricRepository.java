package com.fitwise.repository;

import com.fitwise.entity.RestMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 18/05/20
 */
@Repository
public interface RestMetricRepository extends JpaRepository<RestMetric, Long> {
}
